package io.buildo.toctoc
package slick
package authentication
package login

import zio.{IO, ZIO}
import _root_.slick.jdbc.PostgresProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database

import core.authentication._
import core.authentication.TokenBasedAuthentication._

class PostgreSqlSlickLoginAuthenticationDomain(
  db: Database,
  tableName: String = "login_auth_domain",
  schemaName: Option[String] = None,
) extends LoginDomain
    with BCryptHashing {

  class LoginTable(tag: Tag)
      extends Table[(Int, String, String, String)](tag, schemaName, tableName) {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def ref = column[String]("ref")
    def username = column[String]("username")
    def passwordHash = column[String]("password_hash")

    def * = (id, ref, username, passwordHash)

    def uniqueUsernameIdx =
      index("unique_username_idx", username, unique = true)
  }
  val loginTable = TableQuery[LoginTable]

  override def register(
    s: Subject,
    c: Login,
  ): IO[AuthenticationError, LoginDomain] =
    ZIO.fromFuture { _ =>
      db.run(loginTable += ((0, s.ref, c.username, hashPassword(c.password))))
    }.map(_ => this).mapError(_ => AuthenticationError.InvalidCredential)

  override def unregister(s: Subject): IO[AuthenticationError, LoginDomain] =
    ZIO.fromFuture { _ =>
      db.run(loginTable.filter(_.ref === s.ref).delete)
    }.map(_ => this).orDie

  override def unregister(c: Login): IO[AuthenticationError, LoginDomain] =
    for {
      a <- authenticate(c)
      (_, s) = a
      res <- unregister(s)
    } yield res

  override def authenticate(
    c: Login,
  ): IO[AuthenticationError, (LoginDomain, Subject)] = {
    ZIO.fromFuture { _ =>
      db.run(loginTable.filter(_.username === c.username).result)
    }.orDie.flatMap {
      case l if l.size > 0 =>
        l.find(el => checkPassword(c.password, el._4)) match {
          case Some(el) =>
            ZIO.succeed((this, UserSubject(el._2)))
          case None =>
            ZIO.fail(AuthenticationError.InvalidCredential)
        }
      case _ =>
        ZIO.fail(AuthenticationError.InvalidCredential)
    }
  }
}
