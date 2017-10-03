import java.time.Instant
import java.sql.Timestamp
import scala.concurrent.Future

import Authentication._

object TokenBasedAuthentication {
  case class AccessToken(
    value: String,
    expiresAt: Instant
  ) extends Credentials

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

  // flow authentication goes here

}
