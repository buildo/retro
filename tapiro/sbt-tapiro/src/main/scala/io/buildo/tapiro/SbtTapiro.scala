package io.buildo.tapiro

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import cats.data.NonEmptyList

object SbtTapiro extends AutoPlugin {

  override def requires = JvmPlugin
  override def trigger = noTrigger

  object autoImport {
    val tapiro = taskKey[Unit]("Generate tapir endpoints from controller sources")
    val tapiroRoutesPaths = settingKey[NonEmptyList[String]]("Paths to the controllers describing the routes")
    val tapiroModelsPaths = settingKey[List[String]]("Paths to the models used by the controllers")
    val tapiroOutputPath = settingKey[String]("Path to output generated endpoints")
    val tapiroEndpointsPackages = settingKey[NonEmptyList[String]]("Packages of generate endpoints")
    val tapiroIncludeHttp4sModels = settingKey[Boolean]("Whether to include http4s generated code")
  }

  import autoImport._

  override val globalSettings = Seq(
    tapiro / tapiroRoutesPaths := NonEmptyList("", Nil),
    tapiro / tapiroModelsPaths := Nil,
    tapiro / tapiroOutputPath := "",
    tapiro / tapiroIncludeHttp4sModels := true,
  )

  override val projectSettings = inConfig(Compile)(
    Seq(
      tapiro := {
        Util.createFiles(
          tapiroRoutesPaths.in(tapiro).value.map(s => (scalaSource.value / s).toString),
          tapiroModelsPaths.in(tapiro).value.map(s => (scalaSource.value / s).toString),
          (scalaSource.value / tapiroOutputPath.in(tapiro).value).toString,
          tapiroEndpointsPackages.in(tapiro).value,
          tapiroIncludeHttp4sModels.in(tapiro).value,
        )
      },
    ),
  )

}
