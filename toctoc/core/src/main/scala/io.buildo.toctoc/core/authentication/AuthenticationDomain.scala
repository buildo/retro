package io.buildo.toctoc
package core
package authentication

import cats.data.EitherT
import cats.Monad

trait AuthenticationDomain[F[_], Credential] {
  def authenticate(
    c: Credential,
  ): F[Either[AuthenticationError, (AuthenticationDomain[F, Credential], Subject)]]
  def register(
    s: Subject,
    c: Credential,
  ): F[Either[AuthenticationError, AuthenticationDomain[F, Credential]]]
  def unregister(s: Subject): F[Either[AuthenticationError, AuthenticationDomain[F, Credential]]]
  def unregister(c: Credential): F[Either[AuthenticationError, AuthenticationDomain[F, Credential]]]
}

object AuthenticationDomain {
  def exchangeCredentials[F[_]: Monad, Credential1, Credential2](
    ac: AuthenticationDomain[F, Credential1],
    at: AuthenticationDomain[F, Credential2],
  )(
    c: Credential1,
    t: Credential2,
  ): F[Either[
    AuthenticationError,
    (AuthenticationDomain[F, Credential1], AuthenticationDomain[F, Credential2]),
  ]] =
    (for {
      res <- EitherT(ac.authenticate(c))
      (nac, s) = res
      nat <- EitherT(at.register(s, t))
    } yield (nac, nat)).value
}
