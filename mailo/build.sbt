val akkaVersion = "2.5.18"

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.3"
val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion
val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
val s3 =  "com.github.seratch" %% "awscala" % "0.5.+"
val catsCore = "org.typelevel" %% "cats-core" % "1.0.1"
val alleyCatsCore = "org.typelevel" %% "alleycats-core" % "1.0.1"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.25.2"
val circeCore = "io.circe" %% "circe-core" % "0.11.1"
val circeGeneric = "io.circe" %% "circe-generic" % "0.11.1"
val circeParser = "io.circe" %% "circe-parser" % "0.11.1"
val typesafeConfig = "com.typesafe" % "config" % "1.3.0"
val scalaCacheGuava = "com.github.cb372" %% "scalacache-guava" % "0.9.3"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0"
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
val enumero = "io.buildo" %% "enumero" % "1.3.0"
val levelDb = "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8"
val mailin = "com.sendinblue" % "sib-api-v3-sdk" % "3.0.1"
val akkaPersistenceInMemory = "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.15.1" % Test
val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
val jakartaMail = "com.sun.mail" % "jakarta.mail" % "1.6.3"


val commonSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayOrganization := Some("buildo"),
  organization  := "io.buildo",
  scalaVersion  := "2.12.8",
  releaseEarlyWith := BintrayPublisher,
  releaseEarlyNoGpg := true,
  releaseEarlyEnableSyncToMaven := false,
  developers := List(
    Developer("@danielegallingani",
      "Daniele Gallingani",
      "daniele@buildo.io",
      url("https://buildo.io")),
    Developer("@bytecodeguru",
      "Giuseppe Moscarella",
      "giuseppe.moscarella@buildo.io",
      url("https://buildo.io")),
    Developer("@gabro",
      "Gabriele Petronella",
      "gabriele@buildo.io",
      url("https://buildo.io")),
    Developer("@calippo",
      "Claudio Caletti",
      "claudio@buildo.io",
      url("https://buildo.io"))
  ),
  homepage := Some(url("https://github.com/buildo/mailo")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/buildo/mailo"),
      "scm:git:https://github.com/buildo/mailo.git",
      Some("scm:git:git@github.com:buildo/mailo.git")
    )),
  scalacOptions := Seq(
    "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
    "-language:implicitConversions",
    "-Ypartial-unification"
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.bintrayRepo("buildo", "maven"),
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
    "dnvriend" at "http://dl.bintray.com/dnvriend/maven"
  ),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  fork in run := true,
  fork in Test := true
)

lazy val mailo = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "mailo",
    libraryDependencies ++= Seq(
      scalaTest,
      akkaStream,
      akkaHttp,
      akkaPersistence,
      akkaRemote,
      s3,
      catsCore,
      alleyCatsCore,
      circeCore,
      circeGeneric,
      circeParser,
      typesafeConfig,
      akkaHttpCirce,
      scalaCacheGuava,
      scalaLogging,
      logback,
      mailin,
      enumero,
      levelDb,
      akkaPersistenceInMemory,
      akkaTestkit,
      jakartaMail
    )
  )

mappings in (Compile, packageBin) ~= { _.filter { n =>
  !(n._1.getName.endsWith(".conf.example")) }
}
