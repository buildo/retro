package io.buildo.toctoc
package slick
package authentication
package token

import core.authentication._
import core.authentication.TokenBasedAuthentication._
import PostgreSqlSlickHelper._

import _root_.slick.jdbc.PostgresProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

import java.time.Instant

class PostgreSqlSlickAccessTokenAuthenticationDomain(db: Database)(
    implicit ec: ExecutionContext
) extends AccessTokenAuthenticationDomain {

  class AccessTokenTable(tag: Tag)
      extends Table[(Int, String, String, Instant)](
        tag,
        "access_token_auth_domain"
      ) {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def ref = column[String]("ref")
    def token = column[String]("token")
    def expiresAt = column[Instant]("expires_at")

    def uniqueTokenIdx = index("unique_token_idx", token, unique = true)

    def * = (id, ref, token, expiresAt)
  }
  val accessTokenTable = TableQuery[AccessTokenTable]

  def register(
      s: Subject,
      c: AccessToken
  ): Future[Either[AuthenticationError, AccessTokenDomain]] = {
    db.run(accessTokenTable += ((0, s.ref, c.value, c.expiresAt))) map {
      case _ =>
        Right(this)
    }
  }

  def unregister(
      s: Subject
  ): Future[Either[AuthenticationError, AccessTokenDomain]] =
    db.run(accessTokenTable.filter(_.ref === s.ref).delete) map {
      case _ =>
        Right(this)
    }

  def unregister(
      c: AccessToken
  ): Future[Either[AuthenticationError, AccessTokenDomain]] =
    db.run(accessTokenTable.filter(_.token === c.value).delete) map {
      case _ =>
        Right(this)
    }

  def authenticate(
      c: AccessToken
  ): Future[Either[AuthenticationError, (AccessTokenDomain, Subject)]] = {
    db.run(
      accessTokenTable
        .filter(t => t.token === c.value && t.expiresAt > Instant.now())
        .result
        .headOption
    ) map {
      case None =>
        Left(AuthenticationError.InvalidCredential)
      case Some((_, ref, _, _)) =>
        Right((this, UserSubject(ref)))
      case _ =>
        Left(AuthenticationError.InvalidCredential)
    }
  }
}
