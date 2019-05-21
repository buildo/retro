package io.buildo.toctoc
package slick
package authentication

import core.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import token.PostgreSqlSlickAccessTokenAuthenticationDomain
import login.PostgreSqlSlickLoginAuthenticationDomain

import monix.catnap.FutureLift
import cats.effect.Sync
import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future
import java.time.Duration

class PostgreSqlSlickTokenBasedAuthenticationFlow[F[_]: Sync: FutureLift[?[_], Future]](
  db: Database,
  tokenDuration: Duration = Duration.ofDays(365),
) extends TokenBasedAuthenticationFlow[F](
      loginD = new PostgreSqlSlickLoginAuthenticationDomain[F](db),
      accessTokenD = new PostgreSqlSlickAccessTokenAuthenticationDomain[F](db),
      tokenDuration = tokenDuration,
    )
