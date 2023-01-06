package org.tomasmo.plurpy
package utils

import com.google.protobuf.timestamp.Timestamp

import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDateTime}

//TODO wrap to zio
object TimeConverters {
  def toTimestamp(instant: Instant): Timestamp = {
    val truncatedInstant = truncateToMilis(instant)
    Timestamp(truncatedInstant.getEpochSecond, truncatedInstant.getNano)
  }

  def toTimestamp(localDateTime: LocalDateTime): Timestamp = {
    toTimestamp(localDateTime.toInstant(UTC))
  }

  def toInstant(timestamp: Timestamp): Instant = truncateToMilis(Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos))

  def toLocalDateTime(instant: Instant): LocalDateTime = LocalDateTime.ofInstant(instant, UTC)

  def toLocalDateTime(timestamp: Timestamp): LocalDateTime = toLocalDateTime(toInstant(timestamp))

  private def truncateToMilis(instant: Instant): Instant = instant.truncatedTo(ChronoUnit.MILLIS)
}
