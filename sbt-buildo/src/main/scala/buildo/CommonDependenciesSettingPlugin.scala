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
    val sprayJson = "io.spray" %% "spray-json" % "1.3.2"

    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.0-RC1" % "test"
    val sprayTest = "io.spray" %% "spray-testkit" % "1.3.3" % "test"
    val metarpheusAnnotations = "io.buildo" %% "metarpheus-annotations" % "0.0.1"

    val defaultDependencies = Seq(
      nozzle,
      scalaz,
      slick,
      hikari,
      logging,
      sprayJson,
      metarpheusAnnotations,
      scalaTest,
      sprayTest
    )

  }

  override def trigger = allRequirements
}
