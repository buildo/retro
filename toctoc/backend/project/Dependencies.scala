import sbt._

object Dependencies {
  lazy val slickVersion = "3.2.1"
  lazy val slick = Seq(
    "com.typesafe.slick" %% "slick" % slickVersion,
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  )
  lazy val postgresql = Seq("org.postgresql" % "postgresql" % "9.4.1212")
  lazy val enumero = Seq("io.buildo" %% "enumero" % "1.1.0")
  lazy val wiroServer = Seq("io.buildo" %% "wiro-http-server" % "0.5.2")
  lazy val cats = Seq("org.typelevel" %% "cats-core" % "0.9.0")
  lazy val scalaTestVersion = "3.0.1"
  lazy val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  )
  lazy val bCrypt = Seq(
    "org.mindrot" % "jbcrypt" % "0.4"
  )
}

