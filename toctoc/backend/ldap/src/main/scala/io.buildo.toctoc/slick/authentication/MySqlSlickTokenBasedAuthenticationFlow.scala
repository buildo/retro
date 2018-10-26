package io.buildo.toctoc
package ldap
package authentication

import core.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import token.LdapAccessTokenAuthenticationDomain
import login.LdapLoginAuthenticationDomain
import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext
import java.time.Duration

class LdapTokenBasedAuthenticationFlow(
  db: Database,
  ldapHost: String,
  ldapPort: Int,
  tokenDuration: Duration = Duration.ofDays(365)
)(implicit ec: ExecutionContext)
  extends TokenBasedAuthenticationFlow(
    loginD = new LdapLoginAuthenticationDomain(ldapHost, ldapPort),
    accessTokenD = new LdapAccessTokenAuthenticationDomain(db),
    tokenDuration = tokenDuration
  )
