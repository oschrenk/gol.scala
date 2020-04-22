package gol

import io.circe.generic.auto._

sealed trait Protocol
object Protocol {

  case object Play extends Protocol
  case object Stop extends Protocol
  case object Forward extends Protocol
  case object Back extends Protocol
  case object Pause extends Protocol
  case class Control(width: Int, height: Int, seed: Int, saturation: Double, speed: Int) extends Protocol

  def parse(s: String): Either[Throwable, Protocol] = {
    io.circe.parser
      .parse(s)
      .left
      .map(f => new IllegalArgumentException(f.message))
      .map { json =>
        json.hcursor.downField("event").as[String] match {
          case Left(f) => Left(new IllegalArgumentException(f.message))
          case Right(event) =>
            event match {
              case "play"    => Right(Play)
              case "stop"    => Right(Stop)
              case "pause"   => Right(Pause)
              case "forward" => Right(Forward)
              case "back"    => Right(Back)
              case "control" =>
                json.hcursor
                  .downField("payload")
                  .as[Control]
                  .left
                  .map(f => new IllegalArgumentException(f.message))
              case _ => Left(new IllegalArgumentException("Unknown event"))
            }
        }
      }
      .flatten
  }
}
