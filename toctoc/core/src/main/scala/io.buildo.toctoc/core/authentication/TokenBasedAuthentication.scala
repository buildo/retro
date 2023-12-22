package io.buildo.toctoc
package core
package authentication

import java.time.{Duration, Instant}

import zio.IO

import token.Token

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

  type LoginDomain = AuthenticationDomain[Login]

  case class UserSubject(
    ref: String,
  ) extends Subject

  type AccessTokenDomain = AuthenticationDomain[AccessToken]

  class TokenBasedAuthenticationFlow(
    loginD: LoginDomain,
    accessTokenD: AccessTokenDomain,
    tokenDuration: Duration,
  ) extends BCryptHashing {
    def registerSubjectLogin(s: Subject, l: Login): IO[AuthenticationError, Unit] =
      loginD.register(s, l).unit

    def exchangeForTokens(l: Login): IO[AuthenticationError, AccessToken] = {
      val accessToken = AccessToken.generate(randomString(64), tokenDuration)
      AuthenticationDomain
        .exchangeCredentials(loginD, accessTokenD)(l, accessToken)
        .as(accessToken)
    }

    def validateToken(at: AccessToken): IO[AuthenticationError, Subject] =
      accessTokenD.authenticate(at).map { case (_, s) => s }

    def unregisterToken(at: AccessToken): IO[AuthenticationError, Unit] =
      accessTokenD.unregister(at).unit

    def unregisterAllSubjectTokens(s: Subject): IO[AuthenticationError, Unit] =
      accessTokenD.unregister(s).unit

    def unregisterAllSubjectLogins(s: Subject): IO[AuthenticationError, Unit] =
      loginD.unregister(s).unit

    def unregisterLogin(l: Login): IO[AuthenticationError, Unit] =
      loginD.unregister(l).unit

  }

}
