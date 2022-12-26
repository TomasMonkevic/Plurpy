package org.tomasmo.plurpy
package api

import utils.TimeConverters.toTimestamp
import model.{AuthContext, Account => AccountDto, AccountInfo => AccountInfoDto}
import persistence.AccountsRepositoryImpl
import service.Authorizer
import v1.account.AccountsService.ZioAccountsService.ZAccountsService
import v1.account.AccountsService._
import v1.account.Account.{Account, AccountInfo}

import io.grpc.Status
import zio.{URIO, ZIO}

//TODO later don't depend on specific effect type. Use AccountsRepository
final case class AccountsServiceImpl(
    accountsRepo: AccountsRepositoryImpl,
    authorizer: Authorizer,
) extends ZAccountsService[Any, AuthContext] {
  override def signup(request: SignupRequest): ZIO[Any, Status, SignupResponse] = {
    //TODO input validation and mapper
    val createAccount = for {
      account <- accountsRepo.insert(AccountInfoDto(
        name = request.getAccountInfo.getName,
        passwordHash = request.password //TODO password hashing
      ))
      accessToken <- authorizer.createAccessToken(account.id)
      _ <- ZIO.logInfo(s"Account created. Data(accountId: ${account.id})") //TODO create a wrapper for this data
    } yield SignupResponse(account = Option(toProto(account)), accessToken = accessToken)

    createAccount.orElseFail(Status.INTERNAL)
  }

  override def login(request: LoginRequest): ZIO[Any, Status, LoginResponse] = ???

  override def getAccount(request: GetAccountRequest): ZIO[AuthContext, Status, GetAccountResponse] = for {
    accountId <- authorizer.getAccountId().orElseFail(Status.UNAUTHENTICATED.withDescription("Invalid or missing access token"))
    maybeAccount <- accountsRepo.get(accountId).orElseFail(Status.INTERNAL)
    account <- ZIO.fromOption(maybeAccount).orElseFail(Status.UNAUTHENTICATED.withDescription("Invalid or missing access token"))
    _ <- ZIO.logInfo(s"Account retrieved. Data(accountId: ${account.id})")
  } yield GetAccountResponse(account = Option(toProto(account)))

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
}
