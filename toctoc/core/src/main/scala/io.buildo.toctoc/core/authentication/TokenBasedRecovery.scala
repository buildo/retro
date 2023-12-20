package io.buildo.toctoc
package core
package authentication

import java.time.Duration

import zio.IO

object TokenBasedRecovery {
  import TokenBasedAuthentication._

  final case class TokenBasedRecoveryFlow private (
    loginD: LoginDomain,
    recoveryTokenD: AccessTokenDomain,
    tokenDuration: Duration,
  ) extends BCryptHashing {
    def registerForRecovery(
      s: Subject,
    ): IO[AuthenticationError, (TokenBasedRecoveryFlow, AccessToken)] =
      for {
        rtd <- recoveryTokenD.unregister(s)
        token = AccessToken.generate(randomString(64), tokenDuration)
        rtd2 <- rtd.register(s, token)
        flow = copy(recoveryTokenD = rtd2)
      } yield (flow, token)

    def recoverLogin(token: AccessToken, password: String)(
      usernameForSubject: Subject => IO[AuthenticationError, Option[String]],
    ): IO[AuthenticationError, TokenBasedRecoveryFlow] =
      for {
        authResult <- recoveryTokenD.authenticate(token)
        (rtd, s) = authResult
        username <- usernameForSubject(s).someOrFail(AuthenticationError.InvalidCredential)
        ld <- loginD.unregister(s)
        ld2 <- ld.register(s, Login(username, password))
        rtd2 <- rtd.unregister(s)
        flow = copy(loginD = ld2, recoveryTokenD = rtd2)
      } yield flow

  }

  object TokenBasedRecoveryFlow {
    def create(
      loginD: LoginDomain,
      recoveryTokenD: AccessTokenDomain,
      tokenDuration: Duration,
    ): TokenBasedRecoveryFlow = TokenBasedRecoveryFlow(loginD, recoveryTokenD, tokenDuration)
  }

}
