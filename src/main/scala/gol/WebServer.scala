package gol

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

object WebServer {

  private val DefaultInterface = "0.0.0.0"
  private val DefaultPort = 8080

  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route =
      pathEndOrSingleSlash {
        getFromResource("ui/index.html")
      } ~ pathPrefix("assets") {
        getFromResourceDirectory("ui/assets")
      } ~ path("socket") {
        handleWebSocketMessages(UserFlow.newUserFlow(system.actorOf(UserActor.props())))
      }

    val port = Option.apply(System.getenv("PORT")).flatMap(p => Try(p.toInt).toOption).getOrElse(DefaultPort)

    Http().bindAndHandle(route, DefaultInterface, port)
    println(s"Server online at http://$DefaultInterface:$port/")
  }
}
