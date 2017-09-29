import scala.concurrent.Future
import io.buildo.enumero.annotations.enum

import cats.data.EitherT
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object Authentication {

  abstract trait Credentials {}

  @enum trait AuthenticationError {
    object InvalidAccessToken
    object ExpiredAccessToken
    object InvalidCredentials
  }

  trait Subject {
    def ref: String
  }

  trait AuthenticationDomain[C <: Credentials] {
    def authenticate(c: C): Future[Either[AuthenticationError, (AuthenticationDomain[C], Subject)]]
    def register(s: Subject, c: C): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
    def unregister(s: Subject): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
  }

  def exchangeCredentials[C<: Credentials, C2 <: Credentials](ac: AuthenticationDomain[C], at: AuthenticationDomain[C2])(c: C, t: C2): Future[Either[AuthenticationError, (AuthenticationDomain[C], AuthenticationDomain[C2])]] =
    (for {
      res <- EitherT(ac.authenticate(c))
      (nac, s) = res
      nat <- EitherT(at.register(s, t))
    } yield (nac, nat)).value

}
