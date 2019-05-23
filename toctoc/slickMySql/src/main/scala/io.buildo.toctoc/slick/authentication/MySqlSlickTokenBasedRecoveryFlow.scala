package io.buildo.toctoc
package slick
package authentication

import core.authentication.TokenBasedRecovery.TokenBasedRecoveryFlow
import token.MySqlSlickAccessTokenAuthenticationDomain
import login.MySqlSlickLoginAuthenticationDomain
import _root_.slick.jdbc.JdbcBackend.Database

import cats.effect.Sync
import monix.catnap.FutureLift

import scala.concurrent.Future
import java.time.Duration

class MySqlSlickTokenBasedRecoveryFlow[F[_]: Sync: FutureLift[?[_], Future]](
  db: Database,
  loginTableName: String,
  accessTokenTableName: String,
  tokenDuration: Duration = Duration.ofDays(365),
) extends TokenBasedAuthenticationFlow[F](
      loginD = new MySqlSlickLoginAuthenticationDomain[F](db, loginTableName),
      accessTokenD = new MySqlSlickAccessTokenAuthenticationDomain[F](db, accessTokenTableName),
      tokenDuration = tokenDuration,
    )
