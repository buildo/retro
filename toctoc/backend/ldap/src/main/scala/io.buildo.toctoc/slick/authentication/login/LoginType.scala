package io.buildo.toctoc
package ldap
package authentication
package login

import io.buildo.enumero.annotations.enum

@enum trait LDAPLoginType {
  object DistinguishedName
  object UserPrincipalName
  object Legacy
}
