enablePlugins(GitVersioning)

lazy val commonSettings = Seq(
  organization  := "io.buildo",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  scalaVersion := "2.12.1",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  version       := "0.3.0",
  bintrayOrganization := Some("buildo"),
  bintrayVcsUrl := Some("git@github.com:buildo/java-time-circe-codecs")
)

val circeVersion = "0.11.1"
val circeCore = "io.circe" %% "circe-core" % circeVersion
val circeJava = "io.circe" %% "circe-java8" % circeVersion
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"

lazy val javaTimeCirceCodecs = project
  .settings(commonSettings)
  .settings(
    name := "java-time-circe-codecs",
    libraryDependencies ++= Seq(
      circeCore,
      circeJava,
      scalaTest
    )
  )

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
