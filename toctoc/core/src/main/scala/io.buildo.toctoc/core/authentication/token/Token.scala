package io.buildo.toctoc
package core
package authentication
package token

import java.time.Instant

trait Token[T] {
  def value: T
  def expiresAt: Instant
}
