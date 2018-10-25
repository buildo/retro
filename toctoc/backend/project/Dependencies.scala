import sbt._

object Dependencies {
  val scala211 = "2.11.12"
  val scala212 = "2.12.4"

  val V = new {
    val slick = "3.2.1"
    val quill = "2.3.2"
  }

  val slick = "com.typesafe.slick" %% "slick" % V.slick
  val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % V.slick
  val slf4jNop = "org.slf4j" % "slf4j-nop" % "1.7.25"
  val postgresql = "org.postgresql" % "postgresql" % "9.4.1212"
  val enumero = "io.buildo" %% "enumero" % "1.2.1"
  //Note that wiro 0.6.12 references cats 1.1.0 and circe 0.9.0, that refers to cats 1.0.1, but the two versions of cats
  //are binary compatible by docs: https://github.com/typelevel/cats/releases
  val wiroServer = "io.buildo" %% "wiro-http-server" % "0.6.12"
  val cats = "org.typelevel" %% "cats-core" % "1.1.0"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1"
  val bCrypt = "org.mindrot" % "jbcrypt" % "0.4"
  val quillAsync = "io.getquill" %% "quill-async" % V.quill
  val quillAsyncPostgres = "io.getquill" %% "quill-async-postgres" % V.quill
  val flywayCore = "org.flywaydb" % "flyway-core" % "5.0.7"
  val mysql =  "mysql" % "mysql-connector-java" % "8.0.11"
  val ldap = "com.unboundid" % "unboundid-ldapsdk" % "4.0.8"

  lazy val coreDependencies = List(
    bCrypt,
    cats,
    enumero,
  )

  lazy val slickDependencies = List(
    slick,
    postgresql,
    slickHikari,
  ) ++ List(
    scalatest,
    slf4jNop,
  ).map(_ % Test)

  lazy val slickMySqlDependencies = List(
    slick,
    mysql,
    slickHikari,
  ) ++ List(
    scalatest,
    slf4jNop,
  ).map(_ % Test)

  lazy val quillDependencies = List(
    quillAsync
  ) ++ List(
    scalatest,
    quillAsyncPostgres,
    slf4jNop,
    flywayCore,
    postgresql,
  ).map(_ % Test)

  lazy val wiroDependencies = List(
    wiroServer,
    cats,
  )

  lazy val ldapDependencies = List(
    ldap,
    mysql,
    slick,
    slf4jNop,
    typesafe
  )
}
