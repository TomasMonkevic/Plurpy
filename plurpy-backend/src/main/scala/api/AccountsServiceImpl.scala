package org.tomasmo.plurpy
package api

import utils.TimeConverters.toTimestamp
import utils.JsonUtils.toJson
import utils.TimeProvider
import model.{Account => AccountDto, AccountInfo => AccountInfoDto}
import model.Configs.AuthorizerConfig
import persistence.AccountsRepositoryImpl
import v1.account.AccountsService.ZioAccountsService.ZAccountsService
import v1.account.AccountsService._
import v1.account.Account.{Account, AccountInfo}

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtZIOJson}
import io.grpc.Status
import zio.ZIO

import java.time.temporal.ChronoUnit

//TODO later don't depend on specific effect type. Use AccountsRepository
final case class AccountsServiceImpl(
    accountsRepo: AccountsRepositoryImpl,
    timeProvider: TimeProvider,
    authConfig: AuthorizerConfig, //TODO should the config be in environment? Maye one global config is better?
) extends ZAccountsService[Any, Any] {
  override def signup(request: SignupRequest): ZIO[Any, Status, SignupResponse] = {
    //TODO input validation and mapper
    val foo = for {
      _ <- ZIO.logInfo(s"My secret key I should not show: ${authConfig.key}")
      account <- accountsRepo.insert(AccountInfoDto(
        name = request.getAccountInfo.getName,
        passwordHash = request.password //TODO password hashing
      ))
    } yield SignupResponse(account = Option(toProto(account)), authToken = encode(account)) //TODO maybe don't return auth token on signup only on login?
    foo.orElseFail(Status.INTERNAL) //TODO is this it?
  }

  //TODO move to separate mapper (maybe even automapper)
  private def toProto(accDto: AccountDto): Account = {
    new Account(
      id = Option(accDto.id.toString),
      dateCreated = Option(toTimestamp(accDto.dateCreated)),
      dateUpdated = Option(toTimestamp(accDto.dateUpdated)),
      revision = Option(accDto.revision),
      information = Option(AccountInfo(name = Option(accDto.accountInfo.name))),
    )
  }

  //TODO move to separate service
  private def encode(accDto: AccountDto): String = {
    val now = timeProvider.now()

    val claim = JwtClaim(
      content = toJson(Map("accountId" -> accDto.id)),
      expiration = Some(now.plus(30, ChronoUnit.MINUTES).getEpochSecond),
      issuedAt = Some(now.getEpochSecond)
    )

    JwtZIOJson.encode(claim, authConfig.key, JwtAlgorithm.HS256)
  }

  override def login(request: LoginRequest): ZIO[Any, Status, LoginResponse] = ???

  override def logout(request: LogoutRequest): ZIO[Any, Status, LogoutResponse] = ???
}
