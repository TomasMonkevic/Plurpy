package org.tomasmo.plurpy.utils

import java.time.Instant

trait TimeProvider {
  def now(): Instant
}

class DefaultTimeProvider extends TimeProvider {
  override def now(): Instant = Instant.now()
}
