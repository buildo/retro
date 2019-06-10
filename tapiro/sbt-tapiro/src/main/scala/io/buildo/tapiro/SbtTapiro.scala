package io.buildo.tapiro

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

object SbtTapiro extends AutoPlugin {

  override def requires = JvmPlugin
  override def trigger = noTrigger

  object autoImport {
    val tapiro = taskKey[Unit]("Generate tapir endpoints from controller sources")
    val tapiroControllersPath = settingKey[String]("Path to controller sources")
    val tapiroEndpointsPath = settingKey[String]("Path to output generated endpoints")
    val tapiroEndpointsPackage = settingKey[String]("Package of generate endpoints")
    val tapiroIncludeHttp4sModels = settingKey[Boolean]("Whether to include http4s generated code")
  }

  import autoImport._

  override val globalSettings = Seq(
    tapiro / tapiroControllersPath := "",
    tapiro / tapiroEndpointsPath := "",
    tapiro / tapiroIncludeHttp4sModels := true,
  )

  override val projectSettings = inConfig(Compile)(
    Seq(
      tapiro := {
        Util.createFiles(
          (scalaSource.value / tapiroControllersPath.in(tapiro).value).toString,
          (scalaSource.value / tapiroEndpointsPath.in(tapiro).value).toString,
          tapiroEndpointsPackage.in(tapiro).value,
          tapiroIncludeHttp4sModels.in(tapiro).value,
        )
      },
    ),
  )

}
