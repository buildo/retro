val circeVersion = "0.10.0"
val http4sVersion = "0.20.0-M7"

import cats.data.NonEmptyList
import _root_.io.buildo.tapiro.Server

lazy val root = (project in file("."))
  .enablePlugins(SbtTapiro)
  .settings(
    version := "0.1",
    scalaVersion := "2.12.13",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "1.2.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.12.15",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "0.12.15",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "0.12.15",
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.apache.logging.log4j" % "log4j-core" % "2.13.1",
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
    ).map(_ % circeVersion),
    tapiro / tapiroModelsPaths := List(""),
    tapiro / tapiroRoutesPaths := List(""),
    tapiro / tapiroOutputPath := "endpoints",
    tapiro / tapiroEndpointsPackages := List("endpoints"),
    tapiro / tapiroServer := Server.Http4s,
  )
