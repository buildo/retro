package io.buildo.toctoc
package ldap
package authentication
package login

import core.authentication._

case class LdapLogin(
  username: String,
  password: String
) extends Credential