package io.buildo.toctoc
package core
package authentication

import cats.Monad
import cats.data.EitherT

import java.time.Duration

object TokenBasedRecovery {
  import TokenBasedAuthentication._

  final case class TokenBasedRecoveryFlow[F[_]: Monad] private (
    loginD: LoginDomain[F],
    recoveryTokenD: AccessTokenDomain[F],
    tokenDuration: Duration,
  ) extends BCryptHashing {
    def registerForRecovery(
      s: Subject,
    ): F[Either[AuthenticationError, (TokenBasedRecoveryFlow[F], AccessToken)]] =
      (for {
        rtd <- EitherT(recoveryTokenD.unregister(s))
        token = AccessToken.generate(randomString(64), tokenDuration)
        rtd2 <- EitherT(rtd.register(s, token))
        flow = copy(recoveryTokenD = rtd2)
      } yield (flow, token)).value

    def recoverLogin(token: AccessToken, password: String)(
      usernameForSubject: Subject => F[Option[String]],
    ): F[Either[AuthenticationError, TokenBasedRecoveryFlow[F]]] =
      (for {
        authResult <- EitherT(recoveryTokenD.authenticate(token))
        (rtd, s) = authResult
        username <- EitherT.fromOptionF(
          usernameForSubject(s),
          AuthenticationError.InvalidCredential,
        )
        ld <- EitherT(loginD.unregister(s))
        ld2 <- EitherT(ld.register(s, Login(username, password)))
        rtd2 <- EitherT(rtd.unregister(s))
        flow = copy(loginD = ld2, recoveryTokenD = rtd2)
      } yield flow).value

  }

  object TokenBasedRecoveryFlow {
    def create[F[_]: Monad](
      loginD: LoginDomain[F],
      recoveryTokenD: AccessTokenDomain[F],
      tokenDuration: Duration,
    ): TokenBasedRecoveryFlow[F] = TokenBasedRecoveryFlow(loginD, recoveryTokenD, tokenDuration)
  }

}
