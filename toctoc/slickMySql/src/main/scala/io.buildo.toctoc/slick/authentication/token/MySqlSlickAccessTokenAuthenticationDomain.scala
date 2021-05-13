package io.buildo.toctoc
package slick
package authentication
package token

import core.authentication._
import core.authentication.TokenBasedAuthentication._

import cats.implicits._
import cats.effect.Async
import _root_.slick.jdbc.MySQLProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database

import java.time.Instant
import java.sql.Timestamp

class MySqlSlickAccessTokenAuthenticationDomain[F[_]](
  db: Database,
  tableName: String = "access_token_auth_domain",
  schemaName: Option[String] = None,
)(
  implicit F: Async[F],
) extends AccessTokenDomain[F] {

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
  ): F[Either[AuthenticationError, AccessTokenDomain[F]]] =
    F.fromFuture(F.delay {
        db.run(accessTokenTable += ((0, s.ref, c.value, Timestamp.from(c.expiresAt))))
      })
      .as(this.asRight)

  override def unregister(s: Subject): F[Either[AuthenticationError, AccessTokenDomain[F]]] =
    F.fromFuture(F.delay {
        db.run(accessTokenTable.filter(_.ref === s.ref).delete)
      })
      .as(this.asRight)

  override def unregister(c: AccessToken): F[Either[AuthenticationError, AccessTokenDomain[F]]] =
    F.fromFuture(F.delay {
        db.run(accessTokenTable.filter(_.token === c.value).delete)
      })
      .as(this.asRight)

  override def authenticate(
    c: AccessToken,
  ): F[Either[AuthenticationError, (AccessTokenDomain[F], Subject)]] = {
    F.fromFuture(F.delay {
        db.run(
          accessTokenTable
            .filter(t => t.token === c.value && t.expiresAt > Timestamp.from(Instant.now()))
            .result
            .headOption,
        )
      })
      .map {
        case None =>
          AuthenticationError.InvalidCredential.asLeft
        case Some((_, ref, _, _)) =>
          (this, UserSubject(ref)).asRight
        case _ =>
          AuthenticationError.InvalidCredential.asLeft
      }
  }
}
