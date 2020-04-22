import sbt._

object Dependencies {
  lazy val circeVersion = "0.13.0"
  lazy val akkaHttpVersion = "10.1.11"
  lazy val akkaStreamVersion = "2.6.4"
  lazy val scalaTestVersion = "3.1.1"

  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion

  lazy val circeCore = "io.circe" %% "circe-core" % circeVersion
  lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser = "io.circe" %% "circe-parser" % circeVersion

  lazy val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
}
