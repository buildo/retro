package io.buildo.toctoc
package ldap
package authentication
package login

import io.buildo.toctoc.core.authentication.Subject

case class LdapSubject(
  ref: String
) extends Subject