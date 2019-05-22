package io.buildo.toctoc
package core
package authentication

import cats.Monad
import cats.data.EitherT

import java.time.Duration

object TokenBasedRecovery {
  import TokenBasedAuthentication._

  class TokenBasedRecoveryFlow[F[_]: Monad](
    loginD: LoginAuthenticationDomain[F],
    recoveryTokenD: AccessTokenAuthenticationDomain[F],
    tokenDuration: Duration,
  ) extends BCryptHashing {
    def registerForRecovery(s: Subject): F[Either[AuthenticationError, AccessToken]] =
      (for {
        rtd <- EitherT(recoveryTokenD.unregister(s))
        token = AccessToken.generate(randomString(64), tokenDuration)
        _ <- EitherT(rtd.register(s, token))
      } yield token).value

    def recoverLogin(token: AccessToken, password: String)(
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
