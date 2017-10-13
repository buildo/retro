package io.buildo.toctoc.authentication.token

import java.time.Instant

import io.buildo.toctoc.authentication.Credential

trait Token[T] extends Credential {
  def value: T
  def expiresAt: Instant
}
