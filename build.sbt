import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "dev.oschrenk"
ThisBuild / organizationName := "oschrenk"

mainClass in reStart := Some("gol.WebServer")

lazy val root = (project in file("."))
  .settings(
    name := "gol.scala",
    libraryDependencies ++= Seq(
      akkaHttp,
      akkaStream,
      circeCore,
      circeGeneric,
      scalaTest % Test
    )
  )

