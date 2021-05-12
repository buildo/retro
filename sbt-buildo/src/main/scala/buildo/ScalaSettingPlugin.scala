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

  val baseScalaOptions = Seq(
    "-encoding",
    "utf8",
    "-feature",
    "-deprecation",
    "-language:higherKinds",
    "-language:implicitConversions",
  )

  val baseScala2Options = baseScalaOptions ++ Seq(
    "-unchecked",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-unused",
    "-Yrangepos",
  )

  val baseScala3Options = baseScalaOptions ++ Seq(
    "-Ykind-projector",
  )

  def crossScalacOptions(scalaVersion: String): Seq[String] =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) =>
        baseScala2Options ++
          Seq("-opt-warnings", "-Ypartial-unification", "-Xfuture", "-Ywarn-unused-import")
      case Some((2, 13)) => baseScala2Options ++ Seq("-Ymacro-annotations", "-Ywarn-unused:imports")
      case Some((3, _))  => baseScala3Options
      case _             => Nil
    }

  lazy val baseSettings: Seq[Def.Setting[_]] = Seq(
    cancelable in Global := true,
    scalacOptions ++= crossScalacOptions(scalaVersion.value),
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n < 13 =>
          compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)) :: Nil
        case _ => Nil
      }
    },
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          compilerPlugin(("org.typelevel" % "kind-projector" % "0.11.3").cross(CrossVersion.full)) :: Nil
        case _ => Nil
      }
    },
  )
}
