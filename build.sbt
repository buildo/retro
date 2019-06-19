import Dependencies._
import org.scalajs.sbtplugin.ScalaJSCrossVersion
import sbtcrossproject.CrossPlugin.autoImport.CrossType
import sbtcrossproject.CrossPlugin.autoImport.crossProject

val scala212 = "2.12.12"
val scala213 = "2.13.3"

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
    testFrameworks += new TestFramework("munit.Framework"),
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
    resolvers += Resolver.bintrayRepo("dnvriend", "maven"),
    dynverTagPrefix := "mailo-",
    mappings in (Compile, packageBin) ~= {
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

lazy val docs = project
  .in(file("retro-docs"))
  .settings(
    skip.in(publish) := true,
    moduleName := "retro-docs",
    libraryDependencies ++= docsDependencies,
    mdocVariables := Map(
      "CIRCE_VERSION" -> V.circe,
      "AKKA_HTTP_VERSION" -> V.akkaHttp,
      "TAPIR_VERSION" -> V.tapir,
      "TOCTOC_SNAPSHOT_VERSION" -> version.in(toctocCore).value,
      "TOCTOC_STABLE_VERSION" -> version.in(toctocCore).value.replaceFirst("\\+.*", ""),
      "ENUMERO_SNAPSHOT_VERSION" -> version.in(enumeroCore).value,
      "ENUMERO_STABLE_VERSION" -> version.in(enumeroCore).value.replaceFirst("\\+.*", ""),
      "SBT_BUILDO_SNAPSHOT_VERSION" -> version.in(`sbt-buildo`).value,
      "SBT_BUILDO_STABLE_VERSION" -> version.in(`sbt-buildo`).value.replaceFirst("\\+.*", ""),
      "SBT_TAPIRO_SNAPSHOT_VERSION" -> version.in(`sbt-tapiro`).value,
      "SBT_TAPIRO_STABLE_VERSION" -> version.in(`sbt-tapiro`).value.replaceFirst("\\+.*", ""),
    ),
  )
  .dependsOn(toctocCore, enumeroCore, toctocSlickPostgreSql)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
