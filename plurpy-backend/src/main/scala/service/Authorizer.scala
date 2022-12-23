package org.tomasmo.plurpy
package service

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import utils.JsonUtils.toJson
import utils.TimeProvider
import model.Configs.AuthorizerConfig

import java.io.IOException
import java.time.temporal.ChronoUnit
import zio.{IO, UIO, ZIO}

import java.util.UUID

//TODO write some test at least for this class
case class Authorizer(
    timeProvider: TimeProvider,
    authConfig: AuthorizerConfig, //TODO should the config be in environment? Maye one global config is better?
) {

  private val SigningAlgorithm = JwtAlgorithm.HS256

  def accessToken(accountId: UUID): IO[IOException, String] = for {
    jsonContent <- toJson(Map("accountId" -> accountId))
    token <- createJwtToken(jsonContent)
  } yield token

  private def createJwtToken(content: String): UIO[String] = {
    val now = timeProvider.now()

    val claim = JwtClaim(
      content = content,
      expiration = Option(now.plus(30, ChronoUnit.MINUTES).getEpochSecond), //TODO move token timeout to config
      issuedAt = Option(now.getEpochSecond)
    )

    ZIO.succeed(JwtZIOJson.encode(claim, authConfig.key, SigningAlgorithm))
  }
}
