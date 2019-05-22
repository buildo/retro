package com.geirsson

import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.SbtPgp
import com.typesafe.sbt.SbtPgp.autoImport._
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import sbt.Def
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import sbtdynver.DynVerPlugin
import sbtdynver.DynVerPlugin.autoImport._
import scala.sys.process._
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport._
import scala.util.Try

object CiReleasePlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires =
    JvmPlugin && SbtPgp && DynVerPlugin && GitPlugin && Sonatype

  def tag(prefix: String): Option[String] = {
    val refPath = Paths.get(".git", "ref")
    Try(new String(Files.readAllBytes(refPath))).toOption.filter(_.startsWith(s"${prefix}v"))
  }

  def setupGpg(): Unit = {
    val secret = sys.env("PGP_SECRET")
    (s"echo $secret" #| "gpg --import").!
  }

  override def buildSettings: Seq[Def.Setting[_]] = List(
    pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray()),
  )

  override def globalSettings: Seq[Def.Setting[_]] = List(
    publishArtifact.in(Test) := false,
    publishMavenStyle := true,
  )

  override def projectSettings: Seq[Def.Setting[_]] = List(
    dynverSonatypeSnapshots := true,
    publishConfiguration :=
      publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration :=
      publishLocalConfiguration.value.withOverwrite(true),
    publishTo := sonatypePublishTo.value,
    commands += Command.command("ci-release") { currentState =>
      println("Running ci-release.\n")
      // setupGpg()
      val extracted = Project.extract(currentState)
      val shouldRelease = extracted.structure.allProjectRefs.exists { projectRef =>
        val prefix = extracted.get(dynverTagPrefix.in(projectRef))
        val v = extracted.get(version.in(projectRef))
        tag(prefix).isDefined && !v.endsWith("-SNAPSHOT")
      }
      if (shouldRelease) {
        sys.env.getOrElse("CI_RELEASE", "+publishSigned") ::
          sys.env.getOrElse("CI_SONATYPE_RELEASE", "sonatypeRelease") ::
          currentState
      } else {
        sys.env.getOrElse("CI_RELEASE", "+publishSigned") ::
          currentState
      }
    },
  )

}
