package io.buildo.toctoc
package ldap
package authentication
package login

import io.buildo.enumero.annotations.enum

@enum trait LdapLoginType {
  object DistinguishedName
  object UserPrincipalName
  object Legacy
}
