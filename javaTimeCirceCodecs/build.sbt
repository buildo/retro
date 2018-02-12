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

val circeVersion = "0.9.0"

lazy val javaTimeCirceCodecs = project
  .settings(commonSettings)
  .settings(
    name := "java-time-circe-codecs",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-java8" % circeVersion
    )
  )

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
