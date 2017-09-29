import java.time.Instant
import java.sql.Timestamp
import scala.concurrent.Future

import Authentication._

object TokenBasedAuthentication {
  case class AccessToken(
    value: String,
    expiresAt: Instant
  ) extends Credentials
  // case class RefreshToken(value: String) extends AnyVal

  case class UsernamePassword(
    username: String,
    password: String
  ) extends Credentials

  type UsernamePasswordDomain = AuthenticationDomain[UsernamePassword]

  case class UserSubject(
    ref: String
  ) extends Subject

  trait UsernamePasswordAuthenticationDomain extends UsernamePasswordDomain {
    def authenticate(c: UsernamePassword): Future[Either[AuthenticationError, (UsernamePasswordDomain, Subject)]]
    def register(s: Subject, c: UsernamePassword): Future[Either[AuthenticationError, UsernamePasswordDomain]]
    def unregister(s: Subject): Future[Either[AuthenticationError, UsernamePasswordDomain]]
  }

  type AccessTokenDomain = AuthenticationDomain[AccessToken]
  trait AccessTokenAuthenticationDomain extends AccessTokenDomain {
    def authenticate(c: AccessToken): Future[Either[AuthenticationError, (AccessTokenDomain, Subject)]]
    def register(s: Subject, c: AccessToken): Future[Either[AuthenticationError, AccessTokenDomain]]
    def unregister(s: Subject): Future[Either[AuthenticationError, AccessTokenDomain]]
  }

  import slick.jdbc.PostgresProfile.api._
  import scala.concurrent.ExecutionContext.Implicits.global


  implicit val boolColumnType = MappedColumnType.base[Instant, Timestamp](
    { i => Timestamp.from(i) },
    { t => t.toInstant() }
  )

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

  object SlickAccessTokenAuthenticationDomain extends AccessTokenAuthenticationDomain {
    val db = Database.forConfig("db")

    class AccessTokenTable(tag: Tag) extends Table[(Int, String, String, Instant)](tag, "access_token_auth_domain") {
      def id = column[Int]("id", O.PrimaryKey)
      def ref = column[String]("ref")
      def token = column[String]("token")
      def expiresAt = column[Instant]("expires_at")

      def * = (id, ref, token, expiresAt)
    }
    val tStore = TableQuery[AccessTokenTable]

    def register(s: Subject, c: AccessToken): Future[Either[AuthenticationError, AccessTokenDomain]] = {
      db.run(tStore += (0, s.ref, c.value, c.expiresAt)) map { case _ =>
        Right(this)
      }
    }

    def unregister(s: Subject): Future[Either[AuthenticationError, AccessTokenDomain]] =
      db.run(tStore.filter(_.ref === s.ref).delete) map { case _ =>
        Right(this)
      }

    def authenticate(c: AccessToken): Future[Either[AuthenticationError, (AccessTokenDomain, Subject)]] = {
      db.run(tStore.filter(t => t.token === c.value && t.expiresAt < Instant.now()).result.headOption) map {
        case None =>
          Left(AuthenticationError.InvalidCredentials)
        case Some((_, ref, _, _)) =>
          Right((this, UserSubject(ref)))
        case _ =>
          Left(AuthenticationError.InvalidCredentials)
      }
    }
  }


}
