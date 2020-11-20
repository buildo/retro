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
  )

  def crossFlags(scalaVersion: String): Seq[String] =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) =>
        Seq("-opt-warnings", "-Ypartial-unification", "-Xfuture", "-Ywarn-unused-import")
      case Some((2, 13)) => Seq("-Ymacro-annotations", "-Ywarn-unused:imports")
      case _             => Nil
    }

  lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
    cancelable in Global := true,
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlint",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-unused",
      "-Yrangepos",
    ) ++ crossFlags(scalaVersion.value),
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => Nil
        case _ =>
          compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)) :: Nil
      }
    },
    addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.11.1").cross(CrossVersion.full)),
  )
}
