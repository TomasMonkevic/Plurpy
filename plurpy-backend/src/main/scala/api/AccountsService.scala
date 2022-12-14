package org.tomasmo.plurpy.api

import api.AuthContextTransformer
import io.grpc.Status
import org.tomasmo.plurpy.model.{AuthContext, Account => AccountDto, AccountInfo => AccountInfoDto}
import org.tomasmo.plurpy.persistence.AccountsRepository
import org.tomasmo.plurpy.service.Authorizer
import org.tomasmo.plurpy.utils.TimeConverters.toTimestamp
import org.tomasmo.plurpy.v1.account.Account.{Account, AccountInfo}
import org.tomasmo.plurpy.v1.account.AccountsService.ZioAccountsService.ZAccountsService
import org.tomasmo.plurpy.v1.account.AccountsService._
import zio.{ZIO, ZLayer}

case class AccountsService(
    accountsRepo: AccountsRepository,
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

object AccountsService {
  val live = ZLayer.fromFunction(
    (accountsRepo: AccountsRepository, authorizer: Authorizer) =>
      AccountsService(accountsRepo, authorizer).transformContextZIO(AuthContextTransformer(authorizer))
  )
}
