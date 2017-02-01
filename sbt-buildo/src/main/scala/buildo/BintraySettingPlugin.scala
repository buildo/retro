package buildo

import sbt._
import Keys._
import bintray.BintrayPlugin
import bintray.BintrayPlugin.autoImport._

object BintraySettingPlugin extends AutoPlugin {
  override def requires = plugins.IvyPlugin && BintrayPlugin
  override def trigger = allRequirements

  override def buildSettings: Seq[Def.Setting[_]] = baseBuildSettings
  override def projectSettings: Seq[Def.Setting[_]] = baseSettings

  lazy val baseBuildSettings: Seq[Setting[_]] = Seq(
    developers := List(
      Developer("gabro", "Gabriele Petronella", "@gabro", url("https://github.com/gabro")),
      Developer("calippo", "Claudio Caletti", "", url("https://github.com/calippo"))
    ),
    bintrayReleaseOnPublish := true,
    bintrayOrganization := Some("buildo"),
    bintrayRepository := "maven"
  )

  lazy val baseSettings: Seq[Setting[_]] = Seq(
    bintrayPackage := name.value
  )
}
