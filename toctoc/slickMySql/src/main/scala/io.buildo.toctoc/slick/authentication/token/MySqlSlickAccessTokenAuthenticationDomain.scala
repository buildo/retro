package io.buildo.toctoc
package slick
package authentication
package token

import java.time.Instant
import java.sql.Timestamp

import zio.{IO, ZIO}
import _root_.slick.jdbc.MySQLProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database

import core.authentication._
import core.authentication.TokenBasedAuthentication._

class MySqlSlickAccessTokenAuthenticationDomain(
  db: Database,
  tableName: String = "access_token_auth_domain",
  schemaName: Option[String] = None,
) extends AccessTokenDomain {

  class AccessTokenTable(tag: Tag)
      extends Table[(Int, String, String, Timestamp)](tag, schemaName, tableName) {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def ref = column[String]("ref")
    def token = column[String]("token", O.Length(255))
    def expiresAt = column[Timestamp]("expires_at")

    def uniqueTokenIdx = index("unique_token_idx", token, unique = true)

    def * = (id, ref, token, expiresAt)
  }
  val accessTokenTable = TableQuery[AccessTokenTable]

  override def register(
    s: Subject,
    c: AccessToken,
  ): IO[AuthenticationError, AccessTokenDomain] =
    ZIO.fromFuture { _ =>
      db.run(accessTokenTable += ((0, s.ref, c.value, Timestamp.from(c.expiresAt))))
    }.map(_ => this).mapError(_ => AuthenticationError.InvalidCredential)

  override def unregister(s: Subject): IO[AuthenticationError, AccessTokenDomain] =
    ZIO.fromFuture { _ =>
      db.run(accessTokenTable.filter(_.ref === s.ref).delete)
    }.map(_ => this).mapError(_ => AuthenticationError.Forbidden)

  override def unregister(c: AccessToken): IO[AuthenticationError, AccessTokenDomain] =
    ZIO.fromFuture { _ =>
      db.run(accessTokenTable.filter(_.token === c.value).delete)
    }.map(_ => this).mapError(_ => AuthenticationError.Forbidden)

  override def authenticate(
    c: AccessToken,
  ): IO[AuthenticationError, (AccessTokenDomain, Subject)] = {
    ZIO.fromFuture { _ =>
      db.run(
        accessTokenTable
          .filter(t => t.token === c.value && t.expiresAt > Timestamp.from(Instant.now()))
          .result
          .headOption,
      )
    }.mapError(_ => AuthenticationError.InvalidCredential).flatMap {
      case None =>
        ZIO.fail(AuthenticationError.InvalidCredential)
      case Some((_, ref, _, _)) =>
        ZIO.succeed((this, UserSubject(ref)))
      case _ =>
        ZIO.fail(AuthenticationError.InvalidCredential)
    }
  }
}
