import Dependencies._

organization := "io.buildo"
scalaVersion := "2.12.8"
licenses += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
bintrayOrganization := Some("buildo")
bintrayVcsUrl := Some("git@github.com:buildo/metarpheus-tapir")
scalacOptions := Seq(
  "-Xfatal-warnings",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-feature",
)
libraryDependencies ++= dependencies
