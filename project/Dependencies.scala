import sbt._

object Dependencies {
  val circeVersion = "0.13.0"

  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http"   % "10.1.11"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.6.4"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1"

  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion
}
