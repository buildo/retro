package io.buildo.toctoc
package slick
package authentication
package login

import core.authentication._
import core.authentication.TokenBasedAuthentication._

import cats.data.EitherT
import cats.implicits._
import cats.effect.Sync
import monix.catnap.FutureLift
import monix.catnap.syntax._
import _root_.slick.jdbc.PostgresProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future

class PostgreSqlSlickLoginAuthenticationDomain[F[_]: FutureLift[*[_], Future]](
  db: Database,
  tableName: String = "login_auth_domain",
  schemaName: Option[String] = None,
)(implicit
  F: Sync[F],
) extends LoginDomain[F]
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
  ): F[Either[AuthenticationError, LoginDomain[F]]] =
    F.delay {
      db.run(loginTable += ((0, s.ref, c.username, hashPassword(c.password))))
    }.futureLift
      .as(this.asRight[AuthenticationError])
      .handleError { case _ =>
        AuthenticationError.InvalidCredential.asLeft
      }
      .widen

  override def unregister(s: Subject): F[Either[AuthenticationError, LoginDomain[F]]] =
    F.delay {
      db.run(loginTable.filter(_.ref === s.ref).delete)
    }.futureLift.as(this.asRight)

  override def unregister(c: Login): F[Either[AuthenticationError, LoginDomain[F]]] =
    (for {
      a <- EitherT(authenticate(c))
      (_, s) = a
      res <- EitherT(unregister(s))
    } yield res).value

  override def authenticate(
    c: Login,
  ): F[Either[AuthenticationError, (LoginDomain[F], Subject)]] = {
    F.delay {
      db.run(loginTable.filter(_.username === c.username).result)
    }.futureLift.map {
      case l if l.size > 0 =>
        l.find(el => checkPassword(c.password, el._4)) match {
          case Some(el) =>
            (this, UserSubject(el._2)).asRight
          case None =>
            AuthenticationError.InvalidCredential.asLeft
        }
      case _ =>
        AuthenticationError.InvalidCredential.asLeft
    }
  }
}
