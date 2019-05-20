import sbt._
import sbt.Keys._

object Dependencies {

  val V = new {
    val circe = "0.12.0-M1"
    val scalatest = "3.1.0-SNAP9"
    val mockito = "1.9.5"
    val akka = "2.5.18"
    val akkaHttp = "10.1.3"
    val akkaPersistence = "2.5.15.1"
    val akkaHttpCirce = "1.25.2"
    val awscala = "0.5.+"
    val cats = "1.6.0"
    val config = "1.3.0"
    val scalacacheGuava = "0.9.3"
    val scalaLogging = "3.8.0"
    val logback = "1.2.3"
    val leveldb = "1.8"
    val mailin = "3.0.1"
    val jakartaMail = "1.6.3"
  }

  val circeCore = "io.circe" %% "circe-core" % V.circe
  val circeParser = "io.circe" %% "circe-parser" % V.circe
  val circeGeneric = "io.circe" %% "circe-generic" % V.circe
  val scalatest = "org.scalatest" %% "scalatest" % V.scalatest
  val mockito = "org.mockito" % "mockito-all" % V.mockito
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % V.akka
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % V.akkaHttp
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % V.akka
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % V.akka
  val awscala = "com.github.seratch" %% "awscala" % V.awscala
  val catsCore = "org.typelevel" %% "cats-core" % V.cats
  val alleyCatsCore = "org.typelevel" %% "alleycats-core" % V.cats
  val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % V.akkaHttpCirce
  val typesafeConfig = "com.typesafe" % "config" % V.config
  val scalaCacheGuava = "com.github.cb372" %% "scalacache-guava" % V.scalacacheGuava
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % V.scalaLogging
  val logback = "ch.qos.logback" % "logback-classic" % V.logback
  val levelDb = "org.fusesource.leveldbjni" % "leveldbjni-all" % V.leveldb
  val mailin = "com.sendinblue" % "sib-api-v3-sdk" % V.mailin
  val akkaPersistenceInMemory = "com.github.dnvriend" %% "akka-persistence-inmemory" % V.akkaPersistence
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % V.akka
  val jakartaMail = "com.sun.mail" % "jakarta.mail" % V.jakartaMail

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

  val mailoDependencies = List(
    akkaStream,
    akkaHttp,
    akkaPersistence,
    akkaRemote,
    awscala,
    catsCore,
    alleyCatsCore,
    circeCore,
    circeGeneric,
    circeParser,
    typesafeConfig,
    akkaHttpCirce,
    scalaCacheGuava,
    scalaLogging,
    mailin,
    levelDb,
    jakartaMail
  ) ++ List(
    scalatest,
    logback,
    akkaTestkit,
    akkaPersistenceInMemory
  ).map(_ % Test)

}
