package io.buildo.toctoc
package core
package authentication

import cats.Applicative
import cats.syntax.all._

final class InMemoryAuthenticationDomain[F[_]: Applicative, C](
  private val credentials: Map[C, Subject] = Map.empty,
) extends AuthenticationDomain[F, C] {

  override def authenticate(
    c: C,
  ): F[Either[AuthenticationError, (AuthenticationDomain[F, C], Subject)]] =
    credentials
      .get(c)
      .map((this, _))
      .toRight(AuthenticationError.InvalidCredential)
      .pure[F]
      .widen

  override def register(
    s: Subject,
    c: C,
  ): F[Either[AuthenticationError, AuthenticationDomain[F, C]]] =
    new InMemoryAuthenticationDomain(credentials + (c -> s))
      .asRight[AuthenticationError]
      .pure[F]
      .widen

  override def unregister(s: Subject): F[Either[AuthenticationError, AuthenticationDomain[F, C]]] =
    new InMemoryAuthenticationDomain(credentials.filterNot {
      case (_, v) => v == s
    }).asRight[AuthenticationError]
      .pure[F]
      .widen

  override def unregister(c: C): F[Either[AuthenticationError, AuthenticationDomain[F, C]]] =
    new InMemoryAuthenticationDomain(credentials - c)
      .asRight[AuthenticationError]
      .pure[F]
      .widen
}
