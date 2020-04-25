package gol

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import gol.Protocol._
import gol.UserActor._

object UserActor {
  def props(): Props = Props(new UserActor())

  case class Connected(outActor: ActorRef)
  case object Disconnected
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

  private def toDuration(speed: Int): FiniteDuration = (1000 / speed).milliseconds

  def connected(user: ActorRef): Receive = {
    case c: Control =>
      val rules = Rules(c.tick, c.speed, Seed.random(c.width, c.height, c.seed, c.saturation))

      val originalWorld = World.from(rules.seed)
      val currentWorld = World.tick(rules.tick, originalWorld)

      val originalState = State(originalWorld, rules)
      val currentState = State(currentWorld, rules)

      user ! currentState
      context.become(paused(user, currentState, originalState).orElse(disconnect))
    case Painted(cells) =>
      val rules = Rules(0, 2, Seed.custom(22, 22, cells))
      val world =  World.from(rules.seed)
      val newState = State(world, rules)

      user ! newState
      context.become(paused(user, newState, newState).orElse(disconnect))
  }

  def paused(user: ActorRef, currentState: State, originalState: State): Receive = {
    case Play =>
      val cancellable = context.system.scheduler.scheduleWithFixedDelay(
        Duration.Zero,
        toDuration(originalState.rules.speed),
        self,
        Tick
      )
      context.become(playing(user, cancellable, currentState, originalState).orElse(disconnect))
    case Forward =>
      val newState = currentState.tick()
      user ! newState
      context.become(paused(user, newState, originalState).orElse(disconnect))
    case Back =>
      if (currentState.world.tick > 0) {
        val world = World.tick(currentState.world.tick - 1, originalState.world)
        val newState = State(world, originalState.rules)

        user ! newState
        context.become(paused(user, newState, originalState).orElse(disconnect))
      }
    case Stop =>
      user ! originalState
      context.become(paused(user, originalState, originalState).orElse(disconnect))
    case c: Control =>
      self ! c
      context.become(connected(user).orElse(disconnect))
    case p: Painted =>
      self ! p
      context.become(connected(user).orElse(disconnect))
    case _ => // Pause, Stop do nothing
  }

  def playing(
      user: ActorRef,
      cancellable: Cancellable,
      currentState: State,
      originalState: State
  ): Receive = {
    case Stop =>
      cancellable.cancel()
      user ! originalState
      context.become(paused(user, originalState, originalState).orElse(disconnect))
    case Pause =>
      cancellable.cancel()
      context.become(paused(user, currentState, originalState).orElse(disconnect))
    case Back =>
      cancellable.cancel()
      if (currentState.world.tick > 0) {
        val world = World.tick(currentState.world.tick - 1, originalState.world)
        val newState = State(world, originalState.rules)

        user ! newState
        context.become(paused(user, newState, originalState).orElse(disconnect))
      }
    case Forward =>
      cancellable.cancel()
      val newState = currentState.tick()
      user ! newState
      context.become(paused(user, newState, originalState).orElse(disconnect))
    case Tick =>
      val newState = currentState.tick()
      user ! newState
      context.become(playing(user, cancellable, newState, originalState).orElse(disconnect))
    case c: Control =>
      self ! c
      context.become(connected(user).orElse(disconnect))
    case p: Painted =>
      self ! p
      context.become(connected(user).orElse(disconnect))
    case _ => // Play does nothing
  }

}
