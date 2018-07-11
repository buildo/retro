package io.buildo.toctoc
package slick
package authentication

import core.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import token.MySqlSlickAccessTokenAuthenticationDomain
import login.MySqlSlickLoginAuthenticationDomain
import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext
import java.time.Duration

class MySqlSlickTokenBasedAuthenticationFlow(
  db: Database,
  tokenDuration: Duration = Duration.ofDays(365)
)(implicit ec: ExecutionContext)
  extends TokenBasedAuthenticationFlow(
    loginD = new MySqlSlickLoginAuthenticationDomain(db),
    accessTokenD = new MySqlSlickAccessTokenAuthenticationDomain(db),
    tokenDuration = tokenDuration
  )
