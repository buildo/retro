import sbt._

object Dependencies {
  val scala211 = "2.11.12"
  val scala212 = "2.12.4"

  val V = new {
    val slick = "3.2.1"
  }

  val slick = "com.typesafe.slick" %% "slick" % V.slick
  val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % V.slick
  val slf4jNop = "org.slf4j" % "slf4j-nop" % "1.6.4"
  val postgresql = "org.postgresql" % "postgresql" % "9.4.1212"
  val enumero = "io.buildo" %% "enumero" % "1.1.0"
  val wiroServer = "io.buildo" %% "wiro-http-server" % "0.5.2"
  val cats = "org.typelevel" %% "cats-core" % "0.9.0"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1"
  val bCrypt = "org.mindrot" % "jbcrypt" % "0.4"

  lazy val coreDependencies = List(
    bCrypt,
    cats,
    enumero,
  )

  lazy val slickDependencies = List(
    slick,
    postgresql,
    slickHikari,
  ) ++ List(
    scalatest,
    slf4jNop,
  ).map(_ % Test)

  lazy val wiroDependencies = List(
    wiroServer,
    cats,
  )
}

