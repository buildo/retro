import sbt._

object Dependencies {
  lazy val slick = Seq(
    "com.typesafe.slick" %% "slick" % "3.2.1",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1"
  )
  lazy val postgresql = Seq("org.postgresql" % "postgresql" % "9.4.1212")
  lazy val enumero = Seq("io.buildo" %% "enumero" % "1.1.0")

}

