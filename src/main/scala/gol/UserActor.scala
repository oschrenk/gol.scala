package gol

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import gol.Protocol._
import gol.UserActor._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.Random

object UserActor {
  def props(): Props = Props(new UserActor())

  case class Connected(outActor: ActorRef)
  case object Disconnected
  case class OutgoingMessage(text: String)
  case class IncomingMessage(text: String)
  case object Tick
}

trait Stoppable {
  this: Actor =>
  val disconnect: Receive = {
    case Disconnected =>
      // discard all messages after this
      context.stop(self)
  }
}

class UserActor extends Actor with Stoppable {
  import context.dispatcher

  import scala.concurrent.duration._

  override def receive: Receive = {
    case Connected(user: ActorRef) =>
      context.become(connected(user).orElse(disconnect))
  }

  def connected(user: ActorRef): Receive = {
    case IncomingMessage(text: String) =>
      Protocol.parse(text) match {
        case Right(Control(width, height, seed, saturation, speed)) =>
          val world = World.random(width, height, saturation, new Random(seed))
          val tickDuration = (1000 / speed).milliseconds
          user ! OutgoingMessage(world.asJson.noSpaces)
          context.become(paused(user, world, world, tickDuration).orElse(disconnect))
        case f => println(f)
      }
  }

  def paused(user: ActorRef, currentWorld: World, originalWorld: World, tickDuration: FiniteDuration): Receive = {
    case IncomingMessage(text: String) =>
      Protocol.parse(text) match {
        case Left(t) =>
          println(t.getMessage)
        case Right(protocol) =>
          protocol match {
            case Play =>
              val cancellable = context.system.scheduler.scheduleWithFixedDelay(
                Duration.Zero,
                tickDuration,
                self,
                Tick
              )
              context.become(playing(user, cancellable, currentWorld, originalWorld, tickDuration).orElse(disconnect))
            case Forward =>
              val newWorld = World.tick(currentWorld)
              user ! OutgoingMessage(newWorld.asJson.noSpaces)
              context.become(paused(user, newWorld, originalWorld, tickDuration).orElse(disconnect))
            case Back =>
              if (currentWorld.tick > 0) {
                val newWorld = World.tick(currentWorld.tick - 1, originalWorld)
                user ! OutgoingMessage(newWorld.asJson.noSpaces)
                context.become(paused(user, newWorld, originalWorld, tickDuration).orElse(disconnect))
              }
            case Stop =>
              user ! OutgoingMessage(originalWorld.asJson.noSpaces)
              context.become(paused(user, originalWorld, originalWorld, tickDuration).orElse(disconnect))
            case _ => // Pause, Stop do nothing
          }
      }
  }

  def playing(
      user: ActorRef,
      cancellable: Cancellable,
      currentWorld: World,
      originalWorld: World,
      tickDuration: FiniteDuration
  ): Receive = {
    case IncomingMessage(text: String) =>
      Protocol.parse(text) match {
        case Left(t) => println(t.getMessage)
        case Right(protocol) =>
          protocol match {
            case Stop =>
              cancellable.cancel()
              user ! OutgoingMessage(originalWorld.asJson.noSpaces)
              context.become(paused(user, originalWorld, originalWorld, tickDuration).orElse(disconnect))
            case Pause =>
              cancellable.cancel()
              context.become(paused(user, currentWorld, originalWorld, tickDuration).orElse(disconnect))
            case Back =>
              cancellable.cancel()
              if (currentWorld.tick > 0) {
                val newWorld = World.tick(currentWorld.tick - 1, originalWorld)
                user ! OutgoingMessage(newWorld.asJson.noSpaces)
                context.become(paused(user, newWorld, originalWorld, tickDuration).orElse(disconnect))
              }
            case Forward =>
              cancellable.cancel()
              val newWorld = World.tick(currentWorld)
              user ! OutgoingMessage(newWorld.asJson.noSpaces)
              context.become(paused(user, newWorld, originalWorld, tickDuration).orElse(disconnect))
            case _ => // Play does nothing
          }
      }
    case Tick =>
      val newWorld = World.tick(currentWorld)
      user ! OutgoingMessage(newWorld.asJson.noSpaces)
      context.become(playing(user, cancellable, newWorld, originalWorld, tickDuration).orElse(disconnect))
  }

}
