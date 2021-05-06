import Dependencies._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import org.scalajs.sbtplugin.ScalaJSCrossVersion

val scala212 = "2.12.13"
val scala213 = "2.13.5"

inThisBuild(
  List(
    scalaVersion := scala212,
    // crossScalaVersions := List(scala212, scala213),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    homepage := Some(url("https://github.com/buildo/retro")),
    developers := List(
      Developer(
        "gabro",
        "Gabriele Petronella",
        "gabriele@buildo.io",
        url("https://github.com/gabro"),
      ),
    ),
    scmInfo := Some(
      ScmInfo(
        url(s"https://github.com/buildo/retro"),
        s"scm:git:https://github.com/buildo/retro.git",
        Some(s"scm:git:git@github.com:buildo/retro.git"),
      ),
    ),
    versionScheme := Some("early-semver"),
  ),
)

lazy val `sbt-buildo` = project
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1"),
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0"),
    dynverTagPrefix := "sbt-buildo-",
  )

lazy val enumero = project.aggregate(enumeroCore, enumeroCirce)

lazy val enumeroCore = project
  .in(file("enumero/core"))
  .settings(
    name := "enumero", // TODO(gabro): name consistency
    libraryDependencies ++= enumeroDependencies,
    libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,
    dynverTagPrefix := "enumero-",
  )

lazy val enumeroCirce = project
  .in(file("enumero/circe"))
  .settings(
    name := "enumero-circe-support", // TODO(gabro): name consistency
    libraryDependencies ++= enumeroCirceDependencies,
    dynverTagPrefix := "enumero-",
  )
  .dependsOn(enumeroCore)

lazy val mailo = project
  .settings(
    name := "mailo",
    libraryDependencies ++= mailoDependencies,
    dynverTagPrefix := "mailo-",
    Compile / packageBin / mappings ~= {
      _.filter { n =>
        !(n._1.getName.endsWith(".conf.example"))
      }
    },
  )
  .dependsOn(enumeroCore)

lazy val toctoc =
  project.aggregate(toctocCore, toctocSlickMySql, toctocSlickPostgreSql, toctocLdap, toctocCirce)

lazy val toctocCore = project
  .in(file("toctoc/core"))
  .settings(
    name := "toctoc-core",
    libraryDependencies ++= toctocCoreDependencies,
    dynverTagPrefix := "toctoc-",
  )
  .dependsOn(enumeroCore)

lazy val toctocSlickPostgreSql = project
  .in(file("toctoc/slickPostgreSql"))
  .settings(
    name := "toctoc-slick-postgresql",
    libraryDependencies ++= toctocSlickPostgresDependencies,
    dynverTagPrefix := "toctoc-",
  )
  .dependsOn(toctocCore)

lazy val toctocSlickMySql = project
  .in(file("toctoc/slickMySql"))
  .settings(
    name := "toctoc-slick-mysql",
    libraryDependencies ++= toctocSlickMySqlDependencies,
    dynverTagPrefix := "toctoc-",
  )
  .dependsOn(toctocCore)

lazy val toctocLdap = project
  .in(file("toctoc/ldap"))
  .settings(
    name := "toctoc-ldap",
    libraryDependencies ++= toctocLdapDependencies,
    dynverTagPrefix := "toctoc-",
  )
  .dependsOn(toctocCore)

lazy val toctocCirce = project
  .in(file("toctoc/circe"))
  .settings(
    name := "toctoc-circe",
    dynverTagPrefix := "toctoc-",
    libraryDependencies ++= toctocCirceDependencies,
  )
  .dependsOn(toctocCore)

lazy val metarpheusCore = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("metarpheus/core"))
  .settings(
    name := "metarpheus-core",
    dynverTagPrefix := "metarpheus-",
    libraryDependencies ++= metarpheusCoreDependencies,
  )

lazy val metarpheusJsFacade = project
  .in(file("metarpheus/jsFacade"))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    name := "metarpheus-js-facade",
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= metarpheusJsFacadeDependencies.map(_.cross(ScalaJSCrossVersion.binary)),
    dynverTagPrefix := "metarpheus-",
  )
  .dependsOn(metarpheusCore.js)

lazy val tapiroCore = project
  .in(file("tapiro/core"))
  .settings(
    name := "tapiro-core",
    libraryDependencies ++= tapiroCoreDependencies,
    dynverTagPrefix := "tapiro-",
  )
  .dependsOn(metarpheusCore.jvm)

lazy val `sbt-tapiro` = project
  .in(file("tapiro/sbt-tapiro"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-tapiro",
    dynverTagPrefix := "tapiro-",
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    scripted := {
      (metarpheusCore.jvm / publishLocal).value
      (tapiroCore / publishLocal).value
      scripted.evaluated
    },
  )
  .dependsOn(tapiroCore)

lazy val javaTimeCirceCodecs = project
  .settings(
    name := "java-time-circe-codecs",
    dynverTagPrefix := "java-time-circe-codecs-",
    libraryDependencies ++= javaTimeCirceCodecsDependencies,
  )

lazy val docs = project
  .in(file("retro-docs"))
  .settings(
    publish / skip := true,
    moduleName := "retro-docs",
    libraryDependencies ++= docsDependencies,
    mdocVariables := Map(
      "CIRCE_VERSION" -> V.circe,
      "AKKA_HTTP_VERSION" -> V.akkaHttp,
      "TAPIR_VERSION" -> V.tapir,
      "TOCTOC_SNAPSHOT_VERSION" -> (toctocCore / version).value,
      "TOCTOC_STABLE_VERSION" -> (toctocCore / version).value.replaceFirst("\\+.*", ""),
      "ENUMERO_SNAPSHOT_VERSION" -> (enumeroCore / version).value,
      "ENUMERO_STABLE_VERSION" -> (enumeroCore / version).value.replaceFirst("\\+.*", ""),
      "SBT_BUILDO_SNAPSHOT_VERSION" -> (`sbt-buildo` / version).value,
      "SBT_BUILDO_STABLE_VERSION" -> (`sbt-buildo` / version).value.replaceFirst("\\+.*", ""),
      "SBT_TAPIRO_SNAPSHOT_VERSION" -> (`sbt-tapiro` / version).value,
      "SBT_TAPIRO_STABLE_VERSION" -> (`sbt-tapiro` / version).value.replaceFirst("\\+.*", ""),
      "MAILO_SNAPSHOT_VERSION" -> (mailo / version).value,
      "MAILO_STABLE_VERSION" -> (mailo / version).value.replaceFirst("\\+.*", ""),
    ),
  )
  .dependsOn(toctocCore, enumeroCore, toctocSlickPostgreSql, mailo)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
