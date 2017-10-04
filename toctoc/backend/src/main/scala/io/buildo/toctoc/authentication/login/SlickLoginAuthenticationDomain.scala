package io.buildo.toctoc.authentication.login

import scala.concurrent.Future

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

import io.buildo.toctoc.authentication._
import io.buildo.toctoc.authentication.TokenBasedAuthentication._

object SlickLoginAuthenticationDomain
  extends LoginAuthenticationDomain
  with BCryptHashing {

  val db = Database.forConfig("db")

  class LoginTable(tag: Tag) extends Table[(Int, String, String, String)](tag, "login_auth_domain") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def ref = column[String]("ref")
    def username = column[String]("username")
    def passwordHash = column[String]("password_hash")

    def * = (id, ref, username, passwordHash)
  }
  val loginTable = TableQuery[LoginTable]

  def register(s: Subject, c: Login): Future[Either[AuthenticationError, LoginDomain]] = {
    db.run(loginTable += ((0, s.ref, c.username, hashPassword(c.password)))) map { case _ =>
      Right(this)
    }
  }

  def unregister(s: Subject): Future[Either[AuthenticationError, LoginDomain]] =
    db.run(loginTable.filter(_.ref === s.ref).delete) map { case _ =>
      Right(this)
    }

  def authenticate(c: Login): Future[Either[AuthenticationError, (LoginDomain, Subject)]] = {
    db.run(loginTable.filter(_.username === c.username).result) map {
      case l if l.size > 0 =>
        l.find(el => checkPassword(c.password, el._4)) match {
          case Some(el) =>
            Right((this, UserSubject(el._2)))
          case None =>
            Left(AuthenticationError.InvalidCredentials)
        }
      case _ =>
        Left(AuthenticationError.InvalidCredentials)
    }
  }
}
