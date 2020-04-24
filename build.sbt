import Dependencies._

enablePlugins(DockerPlugin)

lazy val Name = "gameoflife"
lazy val MainClass = "gol.WebServer"

lazy val commonSettings = Seq(
  scalaVersion := "2.13.1",
  version := "0.1.0-SNAPSHOT",
  organization := "dev.oschrenk",
  organizationName := "oschrenk"
)

lazy val assemblySettings = Seq(
  test in assembly := {},
  mainClass in assembly := Some(MainClass),
  assemblyJarName in assembly := "gameoflife.jar"
)

lazy val restartSettings = Seq(
  mainClass in reStart := Some(MainClass)
)

lazy val dockerSettings = Seq(
  dockerfile in docker := {
    // The assembly task generates a fat JAR file
    val artifact: File = assembly.value
    val artifactTargetPath = s"/app/${artifact.name}"

    new Dockerfile {
      from("openjdk:8-jre")
      add(artifact, artifactTargetPath)
      expose(8080)
      entryPoint("java", "-jar", artifactTargetPath)
    }
  },
  imageNames in docker := Seq(
    ImageName(s"${organizationName.value}/${name.value}:latest")
  )
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := Name,
    libraryDependencies ++= Seq(
      akkaHttp,
      akkaStream,
      circeCore,
      circeGeneric,
      circeParser,
      scalaTest % Test
    )
  )
  .settings(restartSettings: _*)
  .settings(assemblySettings: _*)
  .settings(dockerSettings: _*)
