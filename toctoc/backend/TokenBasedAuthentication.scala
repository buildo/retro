import java.time.Instant
import scala.concurrent.Future

import Authentication._

object TokenBasedAuthentication  {
  case class AccessToken(value: String) extends AnyVal
  case class RefreshToken(value: String) extends AnyVal
  case class Token(
    accessToken: AccessToken,
    refreshToken: RefreshToken,
    expiresAt: Instant
  ) extends Credentials

  case class UsernamePassword(
    username: String,
    password: String
  ) extends Credentials

  abstract trait TokenAuthenticationDomain extends AuthenticationDomain[Token]

  trait UsernamePasswordAuthenticationDomain extends AuthenticationDomain[UsernamePassword] {
    def authenticate(c: UsernamePassword): Future[Either[AuthenticationError, (AuthenticationDomain[C], Subject)]]
    def register(s: Subject, c: C): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
    def unregister(s: Subject): Future[Either[AuthenticationError, AuthenticationDomain[C]]]

  }

}
