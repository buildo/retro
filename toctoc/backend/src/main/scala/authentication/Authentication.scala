package authentication

import scala.concurrent.Future

import cats.data.EitherT
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object Authentication {

  def exchangeCredentials[C<: Credential, C2 <: Credential](ac: AuthenticationDomain[C], at: AuthenticationDomain[C2])(c: C, t: C2): Future[Either[AuthenticationError, (AuthenticationDomain[C], AuthenticationDomain[C2])]] =
    (for {
      res <- EitherT(ac.authenticate(c))
      (nac, s) = res
      nat <- EitherT(at.register(s, t))
    } yield (nac, nat)).value

}
