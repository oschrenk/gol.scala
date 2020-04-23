package gol

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContextExecutor

object WebServer {
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

    val interface = "localhost"
    val port = 8080
    Http().bindAndHandle(route, interface, port)
    println(s"Server online at http://$interface:$port/")
  }
}
