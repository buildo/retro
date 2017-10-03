import java.time.Instant
import java.sql.Timestamp
import scala.concurrent.Future

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

import Authentication._
import TokenBasedAuthentication._

import SlickHelper._

object SlickUsernamePasswordAuthenticationDomain extends UsernamePasswordAuthenticationDomain {
  val db = Database.forConfig("db")

  class UsernamePasswordTable(tag: Tag) extends Table[(Int, String, String, String, String)](tag, "username_password_auth_domain") {
    def id = column[Int]("id", O.PrimaryKey)
    def ref = column[String]("ref")
    def username = column[String]("username")
    def passwordHash = column[String]("password_hash")
    def salt = column[String]("salt")

    def * = (id, ref, username, passwordHash, salt)
  }
  val upStore = TableQuery[UsernamePasswordTable]

  private[this] def hashPassword(p: String, s: String) = s"$p$s"
  private[this] def generateSalt() = "asdasd"
  private[this] def checkUsernamePasswordCredentials(clearPassword: String, salt: String, hashedPassword: String) =
    hashPassword(clearPassword, salt) == hashedPassword

  def register(s: Subject, c: UsernamePassword): Future[Either[AuthenticationError, UsernamePasswordDomain]] = {
    val salt = generateSalt()
    db.run(upStore += (0, s.ref, c.username, hashPassword(c.password, salt), salt)) map { case _ =>
      Right(this)
    }
  }

  def unregister(s: Subject): Future[Either[AuthenticationError, UsernamePasswordDomain]] =
    db.run(upStore.filter(_.ref === s.ref).delete) map { case _ =>
      Right(this)
    }

  def authenticate(c: UsernamePassword): Future[Either[AuthenticationError, (UsernamePasswordDomain, Subject)]] = {
    db.run(upStore.filter(_.username === c.username).result.headOption) map {
      case None =>
        Left(AuthenticationError.InvalidCredentials)
      case Some((_, ref, _, passwordHash, salt)) if checkUsernamePasswordCredentials(c.password, salt, passwordHash) =>
        Right((this, UserSubject(ref)))
      case _ =>
        Left(AuthenticationError.InvalidCredentials)
    }
  }
}
