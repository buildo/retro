package io.buildo.toctoc
package core
package authentication

import zio.IO

trait AuthenticationDomain[Credential] {
  def authenticate(
    c: Credential,
  ): IO[AuthenticationError, (AuthenticationDomain[Credential], Subject)]
  def register(
    s: Subject,
    c: Credential,
  ): IO[AuthenticationError, AuthenticationDomain[Credential]]
  def unregister(s: Subject): IO[AuthenticationError, AuthenticationDomain[Credential]]
  def unregister(c: Credential): IO[AuthenticationError, AuthenticationDomain[Credential]]
}

object AuthenticationDomain {
  def exchangeCredentials[Credential1, Credential2](
    ac: AuthenticationDomain[Credential1],
    at: AuthenticationDomain[Credential2],
  )(
    c: Credential1,
    t: Credential2,
  ): IO[
    AuthenticationError,
    (AuthenticationDomain[Credential1], AuthenticationDomain[Credential2]),
  ] =
    for {
      res <- ac.authenticate(c)
      (nac, s) = res
      nat <- at.register(s, t)
    } yield (nac, nat)
}
