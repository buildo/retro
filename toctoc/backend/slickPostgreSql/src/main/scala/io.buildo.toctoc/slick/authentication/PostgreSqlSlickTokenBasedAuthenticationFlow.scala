package io.buildo.toctoc
package slick
package authentication

import core.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import token.PostgreSqlSlickAccessTokenAuthenticationDomain
import login.PostgreSqlSlickLoginAuthenticationDomain

import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext

import java.time.Duration

class PostgreSqlSlickTokenBasedAuthenticationFlow(
  db: Database,
  tokenDuration: Duration = Duration.ofDays(365),
)(implicit ec: ExecutionContext)
    extends TokenBasedAuthenticationFlow(
      loginD = new PostgreSqlSlickLoginAuthenticationDomain(db),
      accessTokenD = new PostgreSqlSlickAccessTokenAuthenticationDomain(db),
      tokenDuration = tokenDuration,
    )
