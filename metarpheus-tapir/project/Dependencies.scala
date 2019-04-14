import sbt._

object Dependencies {
  object versions {
    lazy val scalaMeta = "4.1.0"
    lazy val catsEffect = "1.2.0"
    lazy val scalaFmt = "2.0.0-RC1"
    lazy val tapir = "0.5.2"
    lazy val scala = "2.12.8"
    lazy val scopt = "4.0.0-RC2"
  }

  val dependencies = Seq(
    "org.scalameta" %% "scalameta" % versions.scalaMeta,
    "com.geirsson" %% "scalafmt-core" % versions.scalaFmt,
    "org.scala-lang" % "scala-reflect" % versions.scala,
    "org.typelevel" %% "cats-effect" % versions.catsEffect,
    "com.softwaremill.tapir" %% "tapir-core" % versions.tapir,
    "com.github.scopt" %% "scopt" % versions.scopt,
  )
}
