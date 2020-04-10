package io.buildo.tapiro

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

object SbtTapiro extends AutoPlugin {

  override def requires = JvmPlugin
  override def trigger = noTrigger

  object autoImport {
    val tapiro = taskKey[Unit]("Generate tapir endpoints from controller sources")
    val tapiroRoutesPaths =
      settingKey[List[String]]("Paths to the controllers describing the routes")
    val tapiroModelsPaths = settingKey[List[String]]("Paths to the models used by the controllers")
    val tapiroOutputPath = settingKey[String]("Path to output generated endpoints")
    val tapiroEndpointsPackages = settingKey[List[String]]("Packages of generate endpoints")
    val tapiroServer = settingKey[Server]("For which server generate models (akka-http, http4s...)")
  }

  import autoImport._

  override val globalSettings = Seq(
    tapiro / tapiroRoutesPaths := Nil,
    tapiro / tapiroModelsPaths := Nil,
    tapiro / tapiroOutputPath := "",
    tapiro / tapiroServer := Server.Http4s,
  )

  override val projectSettings = inConfig(Compile)(
    Seq(
      tapiro := {
        new Util().createFiles(
          tapiroRoutesPaths.in(tapiro).value.map(s => (scalaSource.value / s).toString),
          tapiroModelsPaths.in(tapiro).value.map(s => (scalaSource.value / s).toString),
          (scalaSource.value / tapiroOutputPath.in(tapiro).value).toString,
          tapiroEndpointsPackages.in(tapiro).value,
          tapiroServer.in(tapiro).value,
        )
      },
    ),
  )

}
