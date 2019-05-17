import sbt._

object Dependencies {
  val scala212 = "2.12.8"

  val V = new {
    val slick = "3.3.0"
    val catsEffect = "1.3.0"
    val monixCatnap = "3.0.0-RC2"
  }

  val slick = "com.typesafe.slick" %% "slick" % V.slick
  val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % V.slick
  val slf4jNop = "org.slf4j" % "slf4j-nop" % "1.7.25"
  val postgresql = "org.postgresql" % "postgresql" % "42.2.5"
  val enumero = "io.buildo" %% "enumero" % "1.3.0"
  //Note that wiro 0.6.12 references cats 1.1.0 and circe 0.9.0, that refers to cats 1.0.1, but the two versions of cats
  //are binary compatible by docs: https://github.com/typelevel/cats/releases
  val wiroServer = "io.buildo" %% "wiro-http-server" % "0.7.1"
  val cats = "org.typelevel" %% "cats-core" % "1.6.0"
  val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.5"
  val bCrypt = "org.mindrot" % "jbcrypt" % "0.4"
  val flywayCore = "org.flywaydb" % "flyway-core" % "5.2.4"
  val mysql = "mysql" % "mysql-connector-java" % "8.0.15"
  val ldap = "com.unboundid" % "unboundid-ldapsdk" % "4.0.10"
  val monixCatnap = "io.monix" %% "monix-catnap" % V.monixCatnap

  lazy val coreDependencies = List(
    bCrypt,
    cats,
    enumero,
  )

  lazy val slickPostgresDependencies = List(
    slick,
    postgresql,
    slickHikari,
    catsEffect,
    monixCatnap,
  ) ++ List(
    scalatest,
    slf4jNop,
  ).map(_ % Test)

  lazy val slickMySqlDependencies = List(
    slick,
    mysql,
    slickHikari,
    catsEffect,
    monixCatnap,
  ) ++ List(
    scalatest,
    slf4jNop,
  ).map(_ % Test)

  lazy val ldapDependencies = List(
    ldap,
    mysql,
    slick,
    slf4jNop,
    catsEffect,
  )
}
