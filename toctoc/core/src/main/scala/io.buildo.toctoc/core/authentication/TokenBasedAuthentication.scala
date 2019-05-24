package io.buildo.toctoc
package core
package authentication

import token.Token

import cats.implicits._

import java.time.{Duration, Instant}
import cats.Monad

object TokenBasedAuthentication {
  case class AccessToken(
    value: String,
    expiresAt: Instant,
  ) extends Token[String]

  object AccessToken {
    def generate(value: String, duration: Duration): AccessToken =
      AccessToken(value, Instant.now().plus(duration))
  }

  case class RefreshToken(
    value: String,
    expiresAt: Instant,
  ) extends Token[String]

  case class TocTocToken(
    accessToken: AccessToken,
    refreshToken: RefreshToken,
  )

  case class Login(
    username: String,
    password: String,
  )

  type LoginDomain[F[_]] = AuthenticationDomain[F, Login]

  case class UserSubject(
    ref: String,
  ) extends Subject

  type AccessTokenDomain[F[_]] = AuthenticationDomain[F, AccessToken]

  class TokenBasedAuthenticationFlow[F[_]: Monad](
    loginD: LoginDomain[F],
    accessTokenD: AccessTokenDomain[F],
    tokenDuration: Duration,
  ) extends BCryptHashing {
    def registerSubjectLogin(s: Subject, l: Login): F[Either[AuthenticationError, Unit]] =
      loginD.register(s, l).nested.void.value

    def exchangeForTokens(l: Login): F[Either[AuthenticationError, AccessToken]] = {
      val accessToken = AccessToken.generate(randomString(64), tokenDuration)
      AuthenticationDomain
        .exchangeCredentials(loginD, accessTokenD)(l, accessToken)
        .nested
        .as(accessToken)
        .value
    }

    def validateToken(at: AccessToken): F[Either[AuthenticationError, Subject]] =
      accessTokenD.authenticate(at).nested.map { case (_, s) => s }.value

    def unregisterToken(at: AccessToken): F[Either[AuthenticationError, Unit]] =
      accessTokenD.unregister(at).nested.void.value

    def unregisterAllSubjectTokens(s: Subject): F[Either[AuthenticationError, Unit]] =
      accessTokenD.unregister(s).nested.void.value

    def unregisterAllSubjectLogins(s: Subject): F[Either[AuthenticationError, Unit]] =
      loginD.unregister(s).nested.void.value

    def unregisterLogin(l: Login): F[Either[AuthenticationError, Unit]] =
      loginD.unregister(l).nested.void.value

  }

}
