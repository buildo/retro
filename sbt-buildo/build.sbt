enablePlugins(GitVersioning)

lazy val root = (project in file(".")).
  settings(
    sbtPlugin := true,
    name := "sbt-buildo",
    organization := "io.buildo",
    description := "sbt plugin for sharing configuration across projects at buildo",
    addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1"),
    addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.0"),
    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5"),
    licenses := Seq("MIT" -> url("https://github.com/buildo/sbt-buildo/blob/master/LICENSE")),
    scmInfo := Some(ScmInfo(url("https://github.com/buildo/sbt-buildo"), "git@github.com:buildo/sbt-buildo.git")),
    bintrayOrganization := Some("buildo"),
    bintrayRepository := "sbt-plugins"
  )
