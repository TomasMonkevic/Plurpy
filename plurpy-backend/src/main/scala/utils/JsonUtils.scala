package org.tomasmo.plurpy
package utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import zio.{IO, ZIO}

import java.io.IOException

object JsonUtils {
  private val mapper = new ObjectMapper()
    .registerModules(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def toJson(value: Any): IO[IOException, String] = {
    ZIO.attempt(mapper.writeValueAsString(value)).refineToOrDie[IOException]
  }

  def fromJson[T](json: String)(implicit c: Class[T]): IO[JsonProcessingException, T] = {
    ZIO.attempt(mapper.readValue[T](json, c)).refineToOrDie[JsonProcessingException]
  }
}