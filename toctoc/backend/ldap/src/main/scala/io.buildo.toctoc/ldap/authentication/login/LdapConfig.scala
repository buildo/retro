package io.buildo.toctoc
package ldap
package authentication
package login

case class LdapConfig(
  host: String, 
  port: Int,
  loginType: LdapLoginType,
  domain: String,
  distinguishedNameCommonName: String
)
