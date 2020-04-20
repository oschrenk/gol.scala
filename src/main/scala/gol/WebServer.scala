package gol

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.Random

object WebServer {
  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route =
      pathEndOrSingleSlash {
        getFromResource("ui/index.html")
      } ~ pathPrefix("assets") {
        getFromResourceDirectory("ui/assets")
      } ~ path("seed" / LongNumber / DoubleNumber / IntNumber/ IntNumber / IntNumber) { (seed, saturation, width, height, tick) =>
        get {
          complete(HttpEntity(ContentTypes.`application/json`,
            World.tick(tick, World.random(width, height, saturation, new Random(seed))).asJson.spaces2
          ))
        }
      }

    val interface = "localhost"
    val port = 8080
    Http().bindAndHandle(route, interface, port)
    println(s"Server online at http://${interface}:${port}/")
  }
}
