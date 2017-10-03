package io.buildo.toctoc.authentication.login

import java.time.Instant
import java.sql.Timestamp
import scala.concurrent.Future

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

import io.buildo.toctoc.authentication._
import io.buildo.toctoc.authentication.TokenBasedAuthentication._
import io.buildo.toctoc.slick.SlickHelper._

object SlickLoginAuthenticationDomain extends LoginAuthenticationDomain {
  val db = Database.forConfig("db")

  class LoginTable(tag: Tag) extends Table[(Int, String, String, String, String)](tag, "login_auth_domain") {
    def id = column[Int]("id", O.PrimaryKey)
    def ref = column[String]("ref")
    def username = column[String]("username")
    def passwordHash = column[String]("password_hash")
    def salt = column[String]("salt")

    def * = (id, ref, username, passwordHash, salt)
  }
  val upStore = TableQuery[LoginTable]

  private[this] def hashPassword(p: String, s: String) = s"$p$s"
  private[this] def generateSalt() = "asdasd"
  private[this] def checkLoginCredentials(clearPassword: String, salt: String, hashedPassword: String) =
    hashPassword(clearPassword, salt) == hashedPassword

  def register(s: Subject, c: Login): Future[Either[AuthenticationError, LoginDomain]] = {
    val salt = generateSalt()
    db.run(upStore += (0, s.ref, c.username, hashPassword(c.password, salt), salt)) map { case _ =>
      Right(this)
    }
  }

  def unregister(s: Subject): Future[Either[AuthenticationError, LoginDomain]] =
    db.run(upStore.filter(_.ref === s.ref).delete) map { case _ =>
      Right(this)
    }

  def authenticate(c: Login): Future[Either[AuthenticationError, (LoginDomain, Subject)]] = {
    db.run(upStore.filter(_.username === c.username).result.headOption) map {
      case None =>
        Left(AuthenticationError.InvalidCredentials)
      case Some((_, ref, _, passwordHash, salt)) if checkLoginCredentials(c.password, salt, passwordHash) =>
        Right((this, UserSubject(ref)))
      case _ =>
        Left(AuthenticationError.InvalidCredentials)
    }
  }
}
