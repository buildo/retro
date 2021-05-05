package buildo

import sbt._
import Keys._
import sbtassembly.AssemblyPlugin
import sbtassembly.AssemblyPlugin.autoImport._

object AssemblySettingPlugin extends AutoPlugin {
  override def requires = AssemblyPlugin
  override def trigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = baseSettings

  lazy val baseSettings: Seq[Setting[_]] = Seq(
    assembly / test := {},
    assembly / assemblyJarName := s"${name.value}.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("application.conf") => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last == "module-info.class" =>
        MergeStrategy.discard
      case x =>
        val defaultStrategy = (assembly / assemblyMergeStrategy).value
        defaultStrategy(x)
    },
  )
}
