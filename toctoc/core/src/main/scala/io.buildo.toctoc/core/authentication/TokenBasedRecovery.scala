package io.buildo.toctoc
package core
package authentication

import token.Token

import cats.Monad
import cats.data.EitherT

import java.time.{Duration, Instant}

object TokenBasedRecovery {
  case class RecoveryToken(
    value: String,
    expiresAt: Instant,
  ) extends Token[String]

  case class Login(
    username: String,
    password: String,
  ) extends Credential

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

  type RecoveryTokenDomain[F[_]] = AuthenticationDomain[F, RecoveryToken]
  trait RecoveryTokenAuthenticationDomain[F[_]] extends RecoveryTokenDomain[F] {
    def authenticate(
      c: RecoveryToken,
    ): F[Either[AuthenticationError, (RecoveryTokenDomain[F], Subject)]]
    def register(
      s: Subject,
      c: RecoveryToken,
    ): F[Either[AuthenticationError, RecoveryTokenDomain[F]]]
    def unregister(s: Subject): F[Either[AuthenticationError, RecoveryTokenDomain[F]]]
    def unregister(c: RecoveryToken): F[Either[AuthenticationError, RecoveryTokenDomain[F]]]
  }

  class TokenBasedRecoveryFlow[F[_]: Monad](
    loginD: LoginAuthenticationDomain[F],
    recoveryTokenD: RecoveryTokenAuthenticationDomain[F],
    tokenDuration: Duration = Duration.ofDays(365),
  ) extends BCryptHashing {
    def registerForRecovery(s: Subject): F[Either[AuthenticationError, RecoveryToken]] =
      (for {
        rtd <- EitherT(recoveryTokenD.unregister(s))
        token = RecoveryToken(randomString(64), Instant.now().plus(tokenDuration))
        _ <- EitherT(rtd.register(s, token))
      } yield token).value

    def recoverLogin(token: RecoveryToken, password: String)(
      usernameForSubject: Subject => F[Option[String]],
    ): F[Either[AuthenticationError, Unit]] =
      (for {
        authResult <- EitherT(recoveryTokenD.authenticate(token))
        (rtd, s) = authResult
        username <- EitherT.fromOptionF(
          usernameForSubject(s),
          AuthenticationError.InvalidCredential,
        )
        ld <- EitherT(loginD.unregister(s))
        _ <- EitherT(ld.register(s, Login(username, password)))
        _ <- EitherT(rtd.unregister(s))
      } yield ()).value

  }

}
