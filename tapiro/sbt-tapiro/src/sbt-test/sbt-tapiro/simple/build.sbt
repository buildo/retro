val circeVersion = "0.14.4"
val http4sVersion = "0.23.12"
val tapirVersion = "0.20.2"

import cats.data.NonEmptyList

lazy val root = (project in file("."))
  .enablePlugins(SbtTapiro)
  .settings(
    version := "0.1",
    scalaVersion := "2.12.10",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.12",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.apache.logging.log4j" % "log4j-core" % "2.17.2",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
    ),
    tapiro / tapiroModelsPaths := List(""),
    tapiro / tapiroRoutesPaths := List(""),
    tapiro / tapiroOutputPath := "endpoints",
    tapiro / tapiroEndpointsPackages := List("endpoints"),
  )
