package buildo

import sbt._
import Keys._

object ScalaSettingPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  override def buildSettings: Seq[Def.Setting[_]] = baseBuildSettings
  override def projectSettings: Seq[Def.Setting[_]] = baseSettings

  lazy val baseBuildSettings: Seq[Def.Setting[_]] = Seq(
    organization := "io.buildo",
    scalaVersion := "2.11.8"
  )

  lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
    cancelable in Global := true,
    scalacOptions ++= Seq("-encoding", "utf8"),
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint"),
    scalacOptions  += "-language:higherKinds",
    scalacOptions  += "-language:implicitConversions",
    scalacOptions  += "-Xfuture",
    scalacOptions  += "-Yinline-warnings",
    scalacOptions  += "-Yno-adapted-args",
    scalacOptions  += "-Ywarn-dead-code",
    scalacOptions  += "-Ywarn-numeric-widen",
    scalacOptions  += "-Ywarn-value-discard",
    scalacOptions  += "-Ywarn-unused",
    scalacOptions  += "-Ywarn-unused-import"
  )
}

