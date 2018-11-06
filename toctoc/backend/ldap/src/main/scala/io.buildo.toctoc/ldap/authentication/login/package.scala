package io.buildo.toctoc
package ldap
package authentication

import core.authentication._

package object login {
  type LoginDomain = AuthenticationDomain[LdapLogin]
}