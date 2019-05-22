package io.buildo.toctoc
package slick
package authentication

import core.authentication.TokenBasedRecovery.TokenBasedRecoveryFlow
import token.PostgreSqlSlickAccessTokenAuthenticationDomain
import login.PostgreSqlSlickLoginAuthenticationDomain

import monix.catnap.FutureLift
import cats.effect.Sync
import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future
import java.time.Duration

class PostgreSqlSlickTokenBasedRecoveryFlow[F[_]: Sync: FutureLift[?[_], Future]](
  db: Database,
  loginTableName: String,
  recoveryTokenTableName: String,
  tokenDuration: Duration = Duration.ofDays(365),
) extends TokenBasedRecoveryFlow[F](
      loginD = new PostgreSqlSlickLoginAuthenticationDomain[F](db, loginTableName),
      recoveryTokenD =
        new PostgreSqlSlickAccessTokenAuthenticationDomain[F](db, recoveryTokenTableName),
      tokenDuration = tokenDuration,
    )
