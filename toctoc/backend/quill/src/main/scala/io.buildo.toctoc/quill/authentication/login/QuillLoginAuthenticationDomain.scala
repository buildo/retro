package io.buildo.toctoc
package quill
package authentication
package login

import core.authentication._
import core.authentication.TokenBasedAuthentication._

import cats.data.EitherT
import cats.instances.future._
import io.getquill.context.async.AsyncContext
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.NamingStrategy
import com.github.mauricio.async.db.Connection

import scala.concurrent.{ExecutionContext, Future}

class QuillLoginAuthenticationDomain[D <: SqlIdiom, N <: NamingStrategy, C <: Connection](
  ctx: AsyncContext[D, N, C]
)(implicit ec: ExecutionContext)
    extends LoginAuthenticationDomain
    with BCryptHashing {

  import ctx._

  private case class LoginEntity(
    id: Int,
    ref: String,
    username: String,
    passwordHash: String
  )
  private implicit val loginSchemaMeta = schemaMeta[LoginEntity]("LoginAuthDomain")

  def register(s: Subject, c: Login): Future[Either[AuthenticationError, LoginDomain]] =
    run {
      quote {
        query[LoginEntity]
          .insert(lift(LoginEntity(0, s.ref, c.username, hashPassword(c.password))))
          .returning(_.id)
      }
    }.map(_ => Right(this)).recover { case _ => Left(AuthenticationError.InvalidCredential) }

  def unregister(s: Subject): Future[Either[AuthenticationError, LoginDomain]] =
    run {
      quote {
        query[LoginEntity].filter(_.ref == lift(s.ref)).delete
      }
    }.map(_ => Right(this))

  def unregister(c: Login): Future[Either[AuthenticationError, LoginDomain]] =
    (for {
      a <- EitherT(authenticate(c))
      (_, s) = a
      res <- EitherT(unregister(s))
    } yield res).value

  def authenticate(c: Login): Future[Either[AuthenticationError, (LoginDomain, Subject)]] =
    run {
      quote {
        query[LoginEntity].filter(_.username == lift(c.username))
      }
    }.map {
      case l if l.size > 0 =>
        l.find(el => checkPassword(c.password, el.passwordHash)) match {
          case Some(el) =>
            Right((this, UserSubject(el.ref)))
          case None =>
            Left(AuthenticationError.InvalidCredential)
        }
      case _ =>
        Left(AuthenticationError.InvalidCredential)
    }

}
