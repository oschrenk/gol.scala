package gol

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import io.circe.generic.auto._
import io.circe.syntax._

object UserFlow {

  def newUserFlow(userActor: ActorRef): Flow[Message, TextMessage.Strict, NotUsed] = {
    val incomingMessages: Sink[Message, NotUsed] = {
      Flow[Message]
        .map {
          case TextMessage.Strict(text) =>
            Protocol.parse(text) match {
              case Left(t) =>
                println(t.getMessage)
              case Right(protocol) =>
                protocol
            }
        }
        .collect { case p: Protocol => p }
        .to(
          Sink.actorRef(
            userActor,
            UserActor.Disconnected,
            _ => UserActor.Disconnected
          )
        )
    }
    val outgoingMessages = Source
      .actorRef(
        PartialFunction.empty, // never complete
        PartialFunction.empty, // never fail
        10,
        OverflowStrategy.fail
      )
      .mapMaterializedValue { outActor =>
        // give the user actor a way to send messages out
        userActor ! UserActor.Connected(outActor)
        akka.NotUsed
      }
      .map((state: State) => TextMessage(state.asJson.noSpaces))
    Flow.fromSinkAndSourceCoupled(incomingMessages, outgoingMessages)
  }
}
