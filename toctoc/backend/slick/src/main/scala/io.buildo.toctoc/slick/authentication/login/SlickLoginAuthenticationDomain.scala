package io.buildo.toctoc
package slick
package authentication
package login

import core.authentication._
import core.authentication.TokenBasedAuthentication._

import cats.data.EitherT
import cats.implicits._
import _root_.slick.jdbc.PostgresProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class SlickLoginAuthenticationDomain(db: Database)(implicit ec: ExecutionContext)
  extends LoginAuthenticationDomain
  with BCryptHashing {

  class LoginTable(tag: Tag) extends Table[(Int, String, String, String)](tag, "login_auth_domain") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def ref = column[String]("ref")
    def username = column[String]("username")
    def passwordHash = column[String]("password_hash")

    def * = (id, ref, username, passwordHash)

    def uniqueRefIdx = index("unique_ref_idx", username, unique = true)
  }
  val loginTable = TableQuery[LoginTable]

  def register(s: Subject, c: Login): Future[Either[AuthenticationError, LoginDomain]] =
    db.run(loginTable += ((0, s.ref, c.username, hashPassword(c.password)))) map { case _ =>
      Right(this)
    } recover { case _ => Left(AuthenticationError.InvalidCredential) }

  def unregister(s: Subject): Future[Either[AuthenticationError, LoginDomain]] =
    db.run(loginTable.filter(_.ref === s.ref).delete) map { case _ =>
      Right(this)
    }

  def unregister(c: Login): Future[Either[AuthenticationError, LoginDomain]] =
    (for {
      a <- EitherT(authenticate(c))
      (_, s) = a
      res <- EitherT(unregister(s))
    } yield res).value

  def authenticate(c: Login): Future[Either[AuthenticationError, (LoginDomain, Subject)]] = {
    db.run(loginTable.filter(_.username === c.username).result) map {
      case l if l.size > 0 =>
        l.find(el => checkPassword(c.password, el._4)) match {
          case Some(el) =>
            Right((this, UserSubject(el._2)))
          case None =>
            Left(AuthenticationError.InvalidCredential)
        }
      case _ =>
        Left(AuthenticationError.InvalidCredential)
    }
  }
}
