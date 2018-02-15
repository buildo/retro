package io.buildo.toctoc
package quill
package authentication
package token

import core.authentication._
import core.authentication.TokenBasedAuthentication._

import io.getquill.context.async.{AsyncContext, SqlTypes}
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.NamingStrategy
import com.github.mauricio.async.db.Connection
import org.joda.time.{DateTime => JodaDateTime}

import scala.concurrent.{ExecutionContext, Future}

import java.time.Instant

class QuillAccessTokenAuthenticationDomain[D <: SqlIdiom, N <: NamingStrategy, C <: Connection](
  ctx: AsyncContext[D, N, C]
)(implicit ec: ExecutionContext)
    extends AccessTokenAuthenticationDomain {

  import ctx._

  private case class AccessTokenEntity(
    id: Int,
    ref: String,
    token: String,
    expiresAt: Instant
  )
  private implicit val accessTokenSchemaMeta: SchemaMeta[AccessTokenEntity] =
    schemaMeta("AccessTokenAuthdomain")

  private implicit val instantEncoder: Encoder[Instant] =
    encoder[Instant](SqlTypes.TIMESTAMP_WITH_TIMEZONE)
  private implicit val instantDecoder: Decoder[Instant] =
    decoder[Instant]({ case d: JodaDateTime => d.toDate.toInstant }, SqlTypes.TIMESTAMP_WITH_TIMEZONE)

  private implicit class InstantQuotes(left: Instant) {
    def >(right: Instant) = quote(infix"$left > $right".as[Boolean])
    def <(right: Instant) = quote(infix"$left < $right".as[Boolean])
  }

  override def authenticate(
    c: AccessToken
  ): Future[Either[AuthenticationError, (AccessTokenDomain, Subject)]] =
    run {
      quote {
        query[AccessTokenEntity]
          .filter(t => t.token == lift(c.value) && t.expiresAt > lift(Instant.now))
          .take(1)
      }
    }.map(_.headOption match {
      case None                                  => Left(AuthenticationError.InvalidCredential)
      case Some(AccessTokenEntity(_, ref, _, _)) => Right((this, UserSubject(ref)))
      case _                                     => Left(AuthenticationError.InvalidCredential)
    })

  def register(
    s: Subject,
    c: AccessToken
  ): Future[Either[AuthenticationError, AccessTokenDomain]] =
    run {
      quote {
        query[AccessTokenEntity]
          .insert(lift(AccessTokenEntity(0, s.ref, c.value, c.expiresAt)))
          .returning(_.id)
      }
    }.map(_ => Right(this))

  def unregister(s: Subject): Future[Either[AuthenticationError, AccessTokenDomain]] =
    run {
      quote {
        query[AccessTokenEntity].filter(_.ref == lift(s.ref)).delete
      }
    }.map(_ => Right(this))

  def unregister(c: AccessToken): Future[Either[AuthenticationError, AccessTokenDomain]] =
    run {
      quote {
        query[AccessTokenEntity].filter(_.token == lift(c.value)).delete
      }
    }.map(_ => Right(this))

}
