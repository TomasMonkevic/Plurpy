package org.tomasmo.plurpy.utils

import zio.ZLayer

import java.time.Instant

trait TimeProvider {
  def now(): Instant
}

class DefaultTimeProvider extends TimeProvider {
  override def now(): Instant = Instant.now()
}

object TimeProvider {
  val live = ZLayer.succeed(new DefaultTimeProvider)
}
