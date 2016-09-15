package buildo

import sbt._
import Keys._
import org.scalastyle.sbt.ScalastylePlugin._

object LinterSettingPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = baseSettings

  lazy val baseSettings: Seq[Setting[_]] = Seq(
    (scalastyleConfigUrl in Compile) :=
      Some(url("https://raw.githubusercontent.com/buildo/scala-style-guide/master/scalastyle-config.xml"))
  )
}
