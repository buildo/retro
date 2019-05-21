package io.buildo.toctoc
package core
package authentication

import cats.data.EitherT
import cats.Monad

trait AuthenticationDomain[F[_], C <: Credential] {
  def authenticate(c: C): F[Either[AuthenticationError, (AuthenticationDomain[F, C], Subject)]]
  def register(s: Subject, c: C): F[Either[AuthenticationError, AuthenticationDomain[F, C]]]
  def unregister(s: Subject): F[Either[AuthenticationError, AuthenticationDomain[F, C]]]
  def unregister(c: C): F[Either[AuthenticationError, AuthenticationDomain[F, C]]]
}

object AuthenticationDomain {
  def exchangeCredentials[F[_]: Monad, C <: Credential, C2 <: Credential](
    ac: AuthenticationDomain[F, C],
    at: AuthenticationDomain[F, C2],
  )(
    c: C,
    t: C2,
  ): F[Either[AuthenticationError, (AuthenticationDomain[F, C], AuthenticationDomain[F, C2])]] =
    (for {
      res <- EitherT(ac.authenticate(c))
      (nac, s) = res
      nat <- EitherT(at.register(s, t))
    } yield (nac, nat)).value
}
