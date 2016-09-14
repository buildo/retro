package buildo

import sbt._
import Keys._


object CommonDependencySettingPlugin extends AutoPlugin {

  object autoImport {
    val nozzle = "io.buildo" %% "nozzle" % "0.10.7"
    val caseenum =  "io.buildo" %% "ingredients-caseenum" % "0.3.0"
    val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.0"
    val slick = "com.typesafe.slick" %% "slick" % "3.1.1"
    val hikari = "com.typesafe.slick" %% "slick-hikaricp" % "3.1.0"
    val logging = "io.buildo" %% "ingredients-logging" % "0.6.0"

    val defaultDependencies = Seq(
      nozzle,
      scalaz,
      slick,
      hikari,
      logging
    )

  }

  override def trigger = allRequirements
}
