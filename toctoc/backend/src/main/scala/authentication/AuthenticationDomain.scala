package authentication

import scala.concurrent.Future
import cats.data.EitherT
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

trait AuthenticationDomain[C <: Credential] {
  def authenticate(c: C): Future[Either[AuthenticationError, (AuthenticationDomain[C], Subject)]]
  def register(s: Subject, c: C): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
  def unregister(s: Subject): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
}

object AuthenticationDomain {
  def exchangeCredentials[C<: Credential, C2 <: Credential](ac: AuthenticationDomain[C], at: AuthenticationDomain[C2])(c: C, t: C2): Future[Either[AuthenticationError, (AuthenticationDomain[C], AuthenticationDomain[C2])]] =
    (for {
      res <- EitherT(ac.authenticate(c))
      (nac, s) = res
      nat <- EitherT(at.register(s, t))
    } yield (nac, nat)).value
}
