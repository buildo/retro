package authentication

import java.time.Instant
import java.sql.Timestamp
import scala.concurrent.Future

object TokenBasedAuthentication {
  case class AccessToken(
    value: String,
    expiresAt: Instant
  ) extends Credential

  case class Login(
    username: String,
    password: String
  ) extends Credential

  type LoginDomain = AuthenticationDomain[Login]

  case class UserSubject(
    ref: String
  ) extends Subject

  trait LoginAuthenticationDomain extends LoginDomain {
    def authenticate(c: Login): Future[Either[AuthenticationError, (LoginDomain, Subject)]]
    def register(s: Subject, c: Login): Future[Either[AuthenticationError, LoginDomain]]
    def unregister(s: Subject): Future[Either[AuthenticationError, LoginDomain]]
  }

  type AccessTokenDomain = AuthenticationDomain[AccessToken]
  trait AccessTokenAuthenticationDomain extends AccessTokenDomain {
    def authenticate(c: AccessToken): Future[Either[AuthenticationError, (AccessTokenDomain, Subject)]]
    def register(s: Subject, c: AccessToken): Future[Either[AuthenticationError, AccessTokenDomain]]
    def unregister(s: Subject): Future[Either[AuthenticationError, AccessTokenDomain]]
  }

  // flow authentication goes here

}
