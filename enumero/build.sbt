enablePlugins(GitVersioning)

lazy val commonSettings = Seq(
  organization := "io.buildo",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("buildo"),
  bintrayVcsUrl := Some("git@github.com:buildo/enumero"),
  releaseCrossBuild := true,
  addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.0").cross(CrossVersion.full)),
  homepage := Some(url("https://buildo.github.io/enumero")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/buildo/enumero"),
      "scm:git:git@github.com:buildo/enumro.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>gabro</id>
        <name>Gabriele Petronella</name>
        <url>buildo.io</url>
      </developer>
    </developers>
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(core, circeSupport)
  .dependsOn(core, circeSupport)

lazy val core = project
  .settings(commonSettings)
  .settings(
    name := "enumero",
    description := "Beautiful and safe enumerations in Scala",
    libraryDependencies ++= Seq(
      scalaOrganization.value % "scala-reflect" % scalaVersion.value
    ) ++ Seq(
      "org.scalatest" %% "scalatest" % "3.0.1",
      "org.mockito" % "mockito-all" % "1.9.5"
    ).map(_ % Test)
  )

lazy val circeSupport = project
  .settings(commonSettings)
  .settings(
    name := "enumero-circe-support",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.9.0",
      "io.circe" %% "circe-parser" % "0.9.0" % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test
    )
  )
  .dependsOn(core)

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
