enablePlugins(GitVersioning)

lazy val commonSettings = Seq(
  organization  := "io.buildo",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  scalaVersion := "2.12.1",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("buildo")
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(
    name := "enumero",
    libraryDependencies ++= Seq(
      scalaOrganization.value % "scala-reflect" % scalaVersion.value
    ) ++ Seq(
      "org.scalatest"  %% "scalatest"     % "3.0.1",
      "org.mockito"    %  "mockito-all"   % "1.9.5"
    ).map(_ % "test")
  )

lazy val circeSupport = project
  .settings(commonSettings)
  .settings(
    name := "enumero-circe-support",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.7.0"
    )
  )
  .dependsOn(root)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
