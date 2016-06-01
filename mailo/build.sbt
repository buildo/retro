val akkaV = "2.0.3"

val scalaTest = "org.scalatest" %% "scalatest" % "2.1.3" % "test"
val akkaHttpCore = "com.typesafe.akka" % "akka-http-core-experimental_2.11" % akkaV
val akkaHttp = "com.typesafe.akka" % "akka-http-experimental_2.11" % akkaV
val s3 =  "com.github.seratch" %% "awscala" % "0.5.+"
val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.0"
val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.5.2"
val circeCore = "io.circe" %% "circe-core" % "0.3.0"
val circeGeneric = "io.circe" %% "circe-generic" % "0.3.0"
val typesafeConfig = "com.typesafe" % "config" % "1.3.0"

val commonSettings = Seq(
  bintrayOrganization := Some("buildo"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  organization  := "io.buildo",
  version       := "0.1.3",
  scalaVersion  := "2.11.7",
  scalacOptions := Seq(
    "-unchecked", "-deprecation", "-encoding", "utf8", "-feature",
    "-language:implicitConversions"
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.bintrayRepo("buildo", "maven"),
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"
  ),
  fork in run := true
)

lazy val mailo = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "mailo",
    libraryDependencies ++= Seq(
      scalaTest,
      akkaHttp,
      akkaHttpCore,
      s3,
      scalaz,
      akkaHttpCirce,
      circeCore,
      circeGeneric,
      typesafeConfig
    )
  )

mappings in (Compile, packageBin) ~= { _.filter { n =>
  !((n._1.getName.endsWith(".conf")) || n._1.getName.endsWith(".conf.example")) }
}
