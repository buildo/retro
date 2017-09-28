import java.time.Instant
import scala.concurrent.Future

object Authentication {

  abstract trait Credentials {}

  sealed abstract trait AuthenticationError
  case object InvalidAccessToken extends AuthenticationError
  case object ExpiredAccessToken extends AuthenticationError
  case object InvalidCredentials extends AuthenticationError

  trait Subject {
    def ref: String
  }

  trait AuthenticationDomain[C <: Credentials] {
    def get(c: C): Future[Either[AuthenticationError, Subject]]
    def put(s: Subject, c: C): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
  }
  trait Sig[C <: Credentials, C2 <: Credentials] {
    def exchangeCredentials(ac: AuthenticationDomain[C], at: AuthenticationDomain[C2])(c: C, t: C2): Future[Either[AuthenticationError, (AuthenticationDomain[C], AuthenticationDomain[C2])]]
    def authenticate(a: AuthenticationDomain[C])(t: Credentials): Future[Either[AuthenticationError, (Subject, AuthenticationDomain[C])]]
    def register(a: AuthenticationDomain[C])(c: Credentials, s: Subject): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
    def unregister(a: AuthenticationDomain[C])(s: Subject): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
  }

  object TokenBasedAuthentication  {
    case class AccessToken(value: String) extends AnyVal
    case class RefreshToken(value: String) extends AnyVal
    case class Token(
      accessToken: AccessToken,
      refreshToken: RefreshToken,
      expiresAt: Instant
    ) extends Credentials

    case class PasswordCredentials(
      username: String,
      password: String
    ) extends Credentials
  }
}



