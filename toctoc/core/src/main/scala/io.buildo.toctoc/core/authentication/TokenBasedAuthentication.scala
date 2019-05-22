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

  trait LoginAuthenticationDomain[F[_]] extends LoginDomain[F] {
    def authenticate(c: Login): F[Either[AuthenticationError, (LoginDomain[F], Subject)]]
    def register(s: Subject, c: Login): F[Either[AuthenticationError, LoginDomain[F]]]
    def unregister(s: Subject): F[Either[AuthenticationError, LoginDomain[F]]]
    def unregister(c: Login): F[Either[AuthenticationError, LoginDomain[F]]]
  }

  type AccessTokenDomain[F[_]] = AuthenticationDomain[F, AccessToken]
  trait AccessTokenAuthenticationDomain[F[_]] extends AccessTokenDomain[F] {
    def authenticate(
      c: AccessToken,
    ): F[Either[AuthenticationError, (AccessTokenDomain[F], Subject)]]
    def register(s: Subject, c: AccessToken): F[Either[AuthenticationError, AccessTokenDomain[F]]]
    def unregister(s: Subject): F[Either[AuthenticationError, AccessTokenDomain[F]]]
    def unregister(c: AccessToken): F[Either[AuthenticationError, AccessTokenDomain[F]]]
  }

  class TokenBasedAuthenticationFlow[F[_]: Monad](
    loginD: LoginAuthenticationDomain[F],
    accessTokenD: AccessTokenAuthenticationDomain[F],
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
