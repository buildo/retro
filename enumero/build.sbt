enablePlugins(GitVersioning)

lazy val commonSettings = Seq(
  organization  := "io.buildo",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("buildo"),
  bintrayVcsUrl := Some("git@github.com:buildo/enumero"),
  releaseCrossBuild := true
)

lazy val root = project.in(file("."))
  .aggregate(enumero, circeSupport)

lazy val enumero = project.in(file("."))
  .settings(commonSettings)
  .settings(
    name := "enumero",
    description := "Beautiful and safe enumerations in Scala",
    libraryDependencies ++= Seq(
      scalaOrganization.value % "scala-reflect" % scalaVersion.value
    ) ++ Seq(
      "org.scalatest"  %% "scalatest"     % "3.0.1",
      "org.mockito"    %  "mockito-all"   % "1.9.5"
    ).map(_ % Test)
  )

lazy val circeSupport = project
  .settings(commonSettings)
  .settings(
    name := "enumero-circe-support",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.7.0"
    )
  )
  .dependsOn(enumero)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val docs = project
  .enablePlugins(MicrositesPlugin)
  .settings(
    micrositeName := "enumero",
    micrositeDescription := "Beautiful and safe enumerations in Scala",
    micrositeAuthor := "buildo",
    micrositeHomepage := "http://buildo.io",
    micrositeHighlightTheme := "atom-one-light",
    micrositeBaseUrl := "enumero",
    micrositeGithubOwner := "buildo",
    micrositeGithubRepo := "enumero",
    autoAPIMappings := false,
    fork in tut := true,
    git.remoteRepo := "git@github.com:buildo/enumero.git",
    ghpagesNoJekyll := false,
    includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
  )
