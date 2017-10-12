package io.buildo.toctoc.authentication

import slick.jdbc.JdbcBackend.Database

import io.buildo.toctoc.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import io.buildo.toctoc.authentication.token.SlickAccessTokenAuthenticationDomain
import io.buildo.toctoc.authentication.login.SlickLoginAuthenticationDomain

import scala.concurrent.ExecutionContext

import java.time.Duration

class SlickTokenBasedAuthenticationFlow(
  db: Database,
  tokenDuration: Duration = Duration.ofDays(365)
)(implicit ec: ExecutionContext)
  extends TokenBasedAuthenticationFlow(
    loginD = new SlickLoginAuthenticationDomain(db),
    accessTokenD = new SlickAccessTokenAuthenticationDomain(db),
    tokenDuration = tokenDuration
  )
