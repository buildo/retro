package io.buildo.toctoc
package ldap
package authentication
package login

trait LdapLoginType
object LdapLoginType {
  object DistinguishedName extends LdapLoginType
  object UserPrincipalName extends LdapLoginType
  object Legacy extends LdapLoginType
}
