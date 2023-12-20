package io.buildo.toctoc
package core
package authentication
package test

import zio.{IO, ZIO}

final class InMemoryAuthenticationDomain[C] private (
  credentials: Map[C, Subject],
) extends AuthenticationDomain[C] {

  override def authenticate(
    c: C,
  ): IO[AuthenticationError, (AuthenticationDomain[C], Subject)] =
    ZIO
      .fromOption(credentials.get(c).map((this, _)))
      .mapError(_ => AuthenticationError.InvalidCredential)

  override def register(
    s: Subject,
    c: C,
  ): IO[AuthenticationError, AuthenticationDomain[C]] =
    ZIO.succeed(new InMemoryAuthenticationDomain(credentials + (c -> s)))

  override def unregister(s: Subject): IO[AuthenticationError, AuthenticationDomain[C]] =
    ZIO.succeed(new InMemoryAuthenticationDomain(credentials.filterNot { case (_, v) =>
      v == s
    }))

  override def unregister(c: C): IO[AuthenticationError, AuthenticationDomain[C]] =
    ZIO.succeed(new InMemoryAuthenticationDomain(credentials - c))
}

object InMemoryAuthenticationDomain {
  def create[C](
    credentials: Map[C, Subject],
  ): InMemoryAuthenticationDomain[C] =
    new InMemoryAuthenticationDomain[C](credentials)
}
