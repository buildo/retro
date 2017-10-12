package io.buildo.toctoc.authentication

import java.time.{ Instant, Duration }
import scala.concurrent.Future

import cats.data.EitherT
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

object TokenBasedAuthentication {
  case class AccessToken(
    value: String,
    expiresAt: Instant
  ) extends Credential

  case class Login(
    username: String,
    password: String
  ) extends Credential

  type LoginDomain = AuthenticationDomain[Login]

  case class UserSubject(
    ref: String
  ) extends Subject

  trait LoginAuthenticationDomain extends LoginDomain {
    def authenticate(c: Login): Future[Either[AuthenticationError, (LoginDomain, Subject)]]
    def register(s: Subject, c: Login): Future[Either[AuthenticationError, LoginDomain]]
    def unregister(s: Subject): Future[Either[AuthenticationError, LoginDomain]]
    def unregister(c: Login): Future[Either[AuthenticationError, LoginDomain]]
  }

  type AccessTokenDomain = AuthenticationDomain[AccessToken]
  trait AccessTokenAuthenticationDomain extends AccessTokenDomain {
    def authenticate(c: AccessToken): Future[Either[AuthenticationError, (AccessTokenDomain, Subject)]]
    def register(s: Subject, c: AccessToken): Future[Either[AuthenticationError, AccessTokenDomain]]
    def unregister(s: Subject): Future[Either[AuthenticationError, AccessTokenDomain]]
    def unregister(c: AccessToken): Future[Either[AuthenticationError, AccessTokenDomain]]
  }

  class TokenBasedAuthenticationFlow(
    loginD: LoginAuthenticationDomain,
    accessTokenD: AccessTokenAuthenticationDomain,
    tokenDuration: Duration = Duration.ofDays(365)
  ) extends BCryptHashing {
    def registerSubjectLogin(s: Subject, l: Login): Future[Either[AuthenticationError, Unit]] =
      (for {
        _ <- EitherT(loginD.register(s, l))
      } yield ()).value

    def exchangeForTokens(l: Login): Future[Either[AuthenticationError, AccessToken]] = {
      val accessToken = AccessToken(randomString(64), Instant.now().plus(tokenDuration))
      (for {
        _ <- EitherT(AuthenticationDomain.exchangeCredentials(loginD, accessTokenD)(l, accessToken))
      } yield accessToken).value
    }

    def validateToken(at: AccessToken): Future[Either[AuthenticationError, Subject]] =
      (for {
        ref <- EitherT(accessTokenD.authenticate(at))
        (_, s) = ref
      } yield s).value

    def unregisterToken(at: AccessToken): Future[Either[AuthenticationError, Unit]] =
      (for {
        _ <- EitherT(accessTokenD.unregister(at))
      } yield ()).value

    def unregisterAllSubjectTokens(s: Subject): Future[Either[AuthenticationError, Unit]] =
      (for {
        _ <- EitherT(accessTokenD.unregister(s))
      } yield ()).value

    def unregisterAllSubjectLogins(s: Subject): Future[Either[AuthenticationError, Unit]] =
      (for {
        _ <- EitherT(loginD.unregister(s))
      } yield ()).value

    def unregisterLogin(l: Login): Future[Either[AuthenticationError, Unit]] =
      (for {
        _ <- EitherT(loginD.unregister(l))
      } yield ()).value
  }

}
