import Dependencies._

inThisBuild(
  List(
    scalaVersion := scala212,
    resolvers += Resolver.bintrayRepo("buildo", "maven"),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.0").cross(CrossVersion.full)),
    developers := List(
      Developer(
        "@danielegallingani",
        "Daniele Gallingani",
        "daniele@buildo.io",
        url("https://buildo.io"),
      ),
      Developer(
        "@bytecodeguru",
        "Giuseppe Moscarella",
        "giuseppe.moscarella@buildo.io",
        url("https://buildo.io"),
      ),
      Developer("@gabro", "Gabriele Petronella", "gabriele@buildo.io", url("https://buildo.io")),
    ),
    homepage := Some(url("https://github.com/buildo/toctoc")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/buildo/toctoc"),
        "scm:git:https://github.com/buildo/toctoc.git",
        Some("scm:git:git@github.com:buildo/toctoc.git"),
      ),
    ),
    parallelExecution in Test := false,
    addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.10.0").cross(CrossVersion.binary)),
  ),
)

skip.in(publish) := true

lazy val core = project
  .settings(
    name := "toctoc-core",
    libraryDependencies ++= coreDependencies,
  )

lazy val slickPostgreSql = project
  .settings(
    name := "toctoc-slick-postgresql",
    libraryDependencies ++= slickDependencies,
  )
  .dependsOn(core)

lazy val slickMySql = project
  .settings(
    name := "toctoc-slick-mysql",
    libraryDependencies ++= slickMySqlDependencies,
  )
  .dependsOn(core)

lazy val ldap = project
  .settings(
    name := "toctoc-ldap",
    libraryDependencies ++= ldapDependencies,
  )
  .dependsOn(core)

lazy val docs = project
  .in(file("toctoc-docs"))
  .settings(
    skip.in(publish) := true,
    moduleName := "toctoc-docs",
    mdocVariables := Map(
      "VERSION" -> version.value,
      "STABLE_VERSION" -> version.value.replaceFirst("\\+.*", ""),
    ),
  )
  .dependsOn(core)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
