import sbt._
import sbt.Keys._

object Dependencies {

  val V = new {
    val circe = "0.12.0-M1"
    val scalatest = "3.1.0-SNAP9"
  }

  val circeCore = "io.circe" %% "circe-core" % "0.12.0-M1"
  val circeParser = "io.circe" %% "circe-parser" % "0.12.0-M1"
  val scalatest = "org.scalatest" %% "scalatest" % "3.1.0-SNAP9"
  val mockito = "org.mockito" % "mockito-all" % "1.9.5"

  val enumeroDependencies = List(
    scalatest,
    mockito
  ).map(_ % Test)

  val enumeroCirceDependencies = List(
    circeCore
  ) ++ List(
    circeParser,
    scalatest
  ).map(_ % Test)

}
