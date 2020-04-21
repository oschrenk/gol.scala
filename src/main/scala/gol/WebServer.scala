package gol

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContextExecutor
import scala.util.Random

object WebServer {
  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    def newUserFlow(userActor: ActorRef): Flow[Message, TextMessage.Strict, NotUsed] = {
      val incomingMessages: Sink[Message, NotUsed] = {
        Flow[Message]
          .map {
            // transform websocket message to domain message
            case TextMessage.Strict(text) => UserActor.IncomingMessage(text)
            case bm: BinaryMessage        =>
              // ignore binary messages but drain content to avoid the stream being clogged
              bm.dataStream.runWith(Sink.ignore)
              Source.empty
          }
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
        .map(
          // transform domain message to web socket message
          (outMsg: UserActor.OutgoingMessage) => TextMessage(outMsg.text)
        )
      Flow.fromSinkAndSourceCoupled(incomingMessages, outgoingMessages)
    }

    val route =
      pathEndOrSingleSlash {
        getFromResource("ui/index.html")
      } ~ pathPrefix("assets") {
        getFromResourceDirectory("ui/assets")
      } ~ path("seed" / LongNumber / DoubleNumber / IntNumber / IntNumber / IntNumber) {
        (seed, saturation, width, height, tick) =>
          get {
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                World
                  .tick(tick, World.random(width, height, saturation, new Random(seed)))
                  .asJson
                  .spaces2
              )
            )
          }
      } ~ path("socket") {
        handleWebSocketMessages(newUserFlow(system.actorOf(UserActor.props())))
      }

    val interface = "localhost"
    val port = 8080
    Http().bindAndHandle(route, interface, port)
    println(s"Server online at http://$interface:$port/")
  }

}
