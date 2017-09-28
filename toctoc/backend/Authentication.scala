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
    def authenticate(c: C): Future[Either[AuthenticationError, (AuthenticationDomain[C], Subject)]]
    def register(s: Subject, c: C): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
    def unregister(s: Subject): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
  }

  def exchangeCredentials[C<: Credentials, C2 <: Credentials](ac: AuthenticationDomain[C], at: AuthenticationDomain[C2])(c: C, t: C2): Future[Either[AuthenticationError, (AuthenticationDomain[C], AuthenticationDomain[C2])]]

}
