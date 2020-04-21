package gol

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import gol.Protocol.{Play, Stop}
import gol.UserActor._

import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.Random

object UserActor {
  def props(): Props = Props(new UserActor())

  case class Connected(outActor: ActorRef)
  case object Disconnected
  case object Tick
  case class OutgoingMessage(text: String)
  case class IncomingMessage(text: String)
}

class UserActor extends Actor {
  import context.dispatcher
  import scala.concurrent.duration._

  override def receive: Receive = {
    case Connected(user: ActorRef) =>
      context.become(connected(user))
  }

  def connected(user: ActorRef): Receive = {
    case IncomingMessage(text: String) =>
      Protocol.parse(text) match {
        case Left(t) => println(t.getMessage)
        case Right(Play) =>
          val cancellable = context.system.scheduler.scheduleWithFixedDelay(
            Duration.Zero,
            500.milliseconds,
            self,
            Tick
          )
          val width = 22
          val height = 22
          val saturation = 0.5
          val seed = 1234
          val world = World.random(width, height, saturation, new Random(seed))
          context.become(playing(user, cancellable, world))
        case _ => ()
      }
    case Disconnected =>
      // we don't care for other messages after this
      context.stop(self)
  }

  def playing(user: ActorRef, cancellable: Cancellable, world: World): Receive = {
    case IncomingMessage(text: String) =>
      Protocol.parse(text) match {
        case Left(t) => println(t.getMessage)
        case Right(Stop) =>
          cancellable.cancel()
          // TODO reset world
          user ! OutgoingMessage("stop. reset.")
          context.become(connected(user))
      }
    case Tick =>
      val newWorld = World.tick(world)
      user ! OutgoingMessage(newWorld.asJson.spaces2)
      context.become(playing(user, cancellable, newWorld))
    case Disconnected =>
      // we don't care for other messages after this
      context.stop(self)
  }

}
