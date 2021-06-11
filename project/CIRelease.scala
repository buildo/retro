package com.geirsson

import com.typesafe.sbt.GitPlugin
import com.jsuereth.sbtpgp.SbtPgp
import com.jsuereth.sbtpgp.SbtPgp.autoImport._
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

  def tag(prefix: String): Option[String] =
    Try("git tag".!!).toOption.map(_.split("\n")).toList.flatten.find(_.startsWith(s"${prefix}v"))

  def setupGpg(): Unit = {
    val secret = sys.env("PGP_SECRET")
    val passphrase = sys.env("PGP_PASSPHRASE")
    (s"echo $secret" #| "base64 --decode" #| "gzip -d" #| "gpg --batch --import").!
    // The first time you use `gpg --passphrase` the agent prompts for a password nevertheless
    // The `--pinentry-mode loopback` flag seems to do the trick, but we cannot inject it in sbt-pgp
    // so we're running a dummmy gpg signature here so that we won't be asked for the passphrase later
    ("echo dummy" #| s"gpg --pinentry-mode loopback --batch --passphrase $passphrase --no-default-keyring --keyring /root/.gnupg/pubring.kbx --detach-sign --armor --use-agent").!
  }

  override def buildSettings: Seq[Def.Setting[_]] = List(
    pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray()),
  )

  override def globalSettings: Seq[Def.Setting[_]] = List(
    Test / publishArtifact := false,
    publishMavenStyle := true,
  )

  override def projectSettings: Seq[Def.Setting[_]] = List(
    dynverSonatypeSnapshots := true,
    publishConfiguration :=
      publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration :=
      publishLocalConfiguration.value.withOverwrite(true),
    publishTo := sonatypePublishToBundle.value,
    commands += Command.command("ci-release") { currentState =>
      println("Running ci-release.\n")
      setupGpg()
      val extracted = Project.extract(currentState)
      val (releaseProjects, snapshotProjects) =
        extracted.structure.allProjectRefs.partition { projectRef =>
          val prefix = extracted.get(projectRef / dynverTagPrefix)
          val v = extracted.get(projectRef / version)
          tag(prefix).isDefined && !v.endsWith("-SNAPSHOT")
        }

      if (snapshotProjects.length > 0) {
        println("Publishing snapshot version of:")
        println(snapshotProjects.map(_.project).mkString("  - ", "\n  - ", "\n"))
      }
      if (releaseProjects.length > 0) {
        println("Publishing release version of:")
        println(releaseProjects.map(_.project).mkString("  - ", "\n  - ", "\n"))
      }

      val publishSignedCommands =
        releaseProjects.foldLeft(List.empty[String]) { (state, projectRef) =>
          s"+${projectRef.project}/publishSigned" :: state
        }
      val publishCommands = snapshotProjects.foldLeft(List.empty[String]) { (state, projectRef) =>
        s"+${projectRef.project}/publish" :: state
      }

      if (releaseProjects.length > 0) {
        "sonatypeBundleClean" :: publishSignedCommands ::: publishCommands ::: "sonatypeBundleRelease" :: currentState
      } else {
        publishCommands ::: currentState
      }
    },
  )

}
