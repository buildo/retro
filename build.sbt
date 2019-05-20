import Dependencies._

val scala212 = "2.12.8"
val scala213 = "2.13.0-RC2"

inThisBuild(
  List(
    scalaVersion := "2.12.8",
    // crossScalaVersions := List(scala212, scala213),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
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
    }
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
  .settings(
    name := "enumero", // TODO(gabro): name consistency
    libraryDependencies ++= enumeroDependencies,
    libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,
    dynverTagPrefix := "enumero-"
  )

lazy val enumeroCirce = project
  .settings(
    name := "enumero-circe-support", // TODO(gabro): name consistency
    libraryDependencies ++= enumeroCirceDependencies,
    dynverTagPrefix := "enumero-"
  )
  .dependsOn(enumeroCore)
