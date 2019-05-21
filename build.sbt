import Dependencies._

val scala212 = "2.12.8"
val scala213 = "2.13.0-RC2"

inThisBuild(
  List(
    scalaVersion := "2.12.8",
    // crossScalaVersions := List(scala212, scala213),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    homepage := Some(url("https://github.com/buildo/backend")),
    developers := List(
      Developer(
        "gabro",
        "Gabriele Petronella",
        "gabriele@buildo.io",
        url("https://github.com/gabro")
      ),
      Developer(
        "calippo",
        "Claudio Caletti",
        "claudio@buildo.io",
        url("https://github.com/calippo")
      ),
      Developer(
        "bytecodeguru",
        "Giuseppe Moscarella",
        "giuseppe.moscarella@buildo.io",
        url("https://github.com/bytecodeguru")
      )
    ),
    // remove when new sbt-buildo is out
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => "-Ymacro-annotations" :: Nil
        case _                       => Nil
      }
    },
    // remove when new sbt-buildo is out
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => Nil
        case _ =>
          compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)) :: Nil
      }
    },
    addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.10.1").cross(CrossVersion.binary))
  )
)

lazy val `sbt-buildo` = project
  .settings(
    sbtPlugin := true,
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1"),
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9"),
    dynverTagPrefix := "sbt-buildo-"
  )

lazy val enumeroCore = project
  .in(file("enumero/core"))
  .settings(
    name := "enumero", // TODO(gabro): name consistency
    libraryDependencies ++= enumeroDependencies,
    libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,
    dynverTagPrefix := "enumero-"
  )

lazy val enumeroCirce = project
  .in(file("enumero/circe"))
  .settings(
    name := "enumero-circe-support", // TODO(gabro): name consistency
    libraryDependencies ++= enumeroCirceDependencies,
    dynverTagPrefix := "enumero-"
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
    }
  )
  .dependsOn(enumeroCore)

lazy val toctocCore = project
  .in(file("toctoc/core"))
  .settings(
    name := "toctoc-core",
    libraryDependencies ++= toctocCoreDependencies,
    dynverTagPrefix := "toctoc-"
  )
  .dependsOn(enumeroCore)

lazy val toctocSlickPostgreSql = project
  .in(file("toctoc/slickPostgreSql"))
  .settings(
    name := "toctoc-slick-postgresql",
    libraryDependencies ++= toctocSlickPostgresDependencies,
    dynverTagPrefix := "toctoc-"
  )
  .dependsOn(toctocCore)

lazy val toctocSlickMySql = project
  .in(file("toctoc/slickMySql"))
  .settings(
    name := "toctoc-slick-mysql",
    libraryDependencies ++= toctocSlickMySqlDependencies,
    dynverTagPrefix := "toctoc-"
  )
  .dependsOn(toctocCore)

lazy val toctocLdap = project
  .in(file("toctoc/ldap"))
  .settings(
    name := "toctoc-ldap",
    libraryDependencies ++= toctocLdapDependencies,
    dynverTagPrefix := "toctoc-"
  )
  .dependsOn(toctocCore)
