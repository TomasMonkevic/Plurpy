package org.tomasmo.plurpy.service

import domain.CommonTypes.AccountId
import org.tomasmo.plurpy.domain.AuthContext
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import org.tomasmo.plurpy.utils.TimeProvider
import org.tomasmo.plurpy.domain.Configs.AuthorizerConfig

import java.io.IOException
import java.time.temporal.ChronoUnit
import zio.{IO, Task, UIO, ZIO, ZLayer}
import zio.json._

//TODO move to a separate file
class UnauthenticatedException() extends Exception("Invalid or missing access token") {}

trait Authorizer {
  def createAccessToken(accountId: AccountId): IO[IOException, String]

  def getAuthContext(accessToken: String): UIO[AuthContext]

  def getAccountId(): ZIO[AuthContext, UnauthenticatedException, AccountId]
}

//TODO write some test at least for this class
case class AuthorizerImpl(
    timeProvider: TimeProvider,
    authConfig: AuthorizerConfig, //TODO should the config be in environment? Maye one global config is better?
) extends Authorizer {

  private val SigningAlgorithm = JwtAlgorithm.HS256

  def createAccessToken(accountId: AccountId): IO[IOException, String] = {
    val authContextJson = AuthContext(accountId = Option(accountId)).toJson
    createJwtToken(authContextJson)
  }

  def getAuthContext(accessToken: String): UIO[AuthContext] = {
    val getAuthContext = for {
      jwtClaim <- decodeJwtToken(accessToken)
      accountContext <- ZIO.fromEither(jwtClaim.content.fromJson[AuthContext])
    } yield accountContext

    getAuthContext.orElse(ZIO.succeed(AuthContext.empty))
  }

  def getAccountId(): ZIO[AuthContext, UnauthenticatedException, AccountId] = for {
    maybeAccountId <- ZIO.service[AuthContext].map(_.accountId)
    accountId <- ZIO.fromOption(maybeAccountId).orElseFail(new UnauthenticatedException)
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

object Authorizer {
  val live = ZLayer.fromFunction(AuthorizerImpl(_, _))
}
