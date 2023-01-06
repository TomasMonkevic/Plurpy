package org.tomasmo.plurpy
package service

import model.AuthContext

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import utils.JsonUtils.{fromJson, toJson}
import utils.TimeProvider
import model.Configs.AuthorizerConfig

import java.io.IOException
import java.time.temporal.ChronoUnit
import zio.{IO, Task, UIO, ZIO}

import java.util.UUID

//TODO move to a separate file
class UnauthenticatedException() extends Exception("Invalid or missing access token") {}

trait AuthorizerTrait[E] {
  def createAccessToken(accountId: UUID): IO[E, String]

  def getAuthContext(accessToken: String): UIO[AuthContext]

  def getAccountId(): ZIO[AuthContext, E, UUID]
}

object AuthorizerTrait {
  sealed trait AuthorizerError

  case class ReasonA(msg: String) extends AuthorizerError

  case class ReasonB(msg: String) extends AuthorizerError

  def mapError[E1, E2](authorizer: AuthorizerTrait[E1])(func: E1 => E2): AuthorizerTrait[E2] = {
    new AuthorizerTrait[E2] {
      override def createAccessToken(accountId: UUID): IO[E2, String] = {
        authorizer.createAccessToken(accountId).mapError(func)
      }

      override def getAuthContext(accessToken: String): UIO[AuthContext] = {
        authorizer.getAuthContext(accessToken)
      }

      override def getAccountId(): ZIO[AuthContext, E2, UUID] = {
        authorizer.getAccountId().mapError(func)
      }
    }
  }
}

//TODO write some test at least for this class
case class Authorizer(
    timeProvider: TimeProvider,
    authConfig: AuthorizerConfig, //TODO should the config be in environment? Maye one global config is better?
) extends AuthorizerTrait[AuthorizerTrait.AuthorizerError] {

  private val SigningAlgorithm = JwtAlgorithm.HS256

  def createAccessToken(accountId: UUID): IO[AuthorizerTrait.ReasonA, String] = for {
    jsonContent <- toJson(AuthContext(accountId = Option(accountId))).mapError(ex => AuthorizerTrait.ReasonA(ex.getMessage))
    token <- createJwtToken(jsonContent)
  } yield token

  def getAuthContext(accessToken: String): UIO[AuthContext] = {
    val getAuthContext = for {
      jwtClaim <- decodeJwtToken(accessToken)
      accountContext <- fromJson(jwtClaim.content)(classOf[AuthContext])
    } yield accountContext

    getAuthContext.orElse(ZIO.succeed(AuthContext.empty))
  }

  def getAccountId(): ZIO[AuthContext, AuthorizerTrait.ReasonB, UUID] = for {
    maybeAccountId <- ZIO.service[AuthContext].map(_.accountId)
    accountId <- ZIO.fromOption(maybeAccountId).orElseFail(AuthorizerTrait.ReasonB("some"))
  } yield accountId

  private def createJwtToken(content: String): UIO[String] = {
    val now = timeProvider.now()

    val claim = JwtClaim(
      content = content,
      expiration = Option(now.plus(30, ChronoUnit.MINUTES).getEpochSecond), //TODO move token timeout to config
      issuedAt = Option(now.getEpochSecond)
    )

    ZIO.succeed(JwtZIOJson.encode(claim, authConfig.key, SigningAlgorithm))
  }

  private def decodeJwtToken(jwtToken: String): Task[JwtClaim] = {
    ZIO.fromTry(JwtZIOJson.decode(jwtToken, authConfig.key, Seq(SigningAlgorithm)))
  }
}
