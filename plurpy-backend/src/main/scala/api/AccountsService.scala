package org.tomasmo.plurpy.api

import api.AuthContextTransformer
import domain.CommonTypes.Name
import io.grpc.Status
import org.tomasmo.plurpy.domain.{AuthContext, Account => AccountDto, AccountInfo => AccountInfoDto}
import org.tomasmo.plurpy.repository.AccountsRepository
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
    //TODO don't return the raw response from refined leaking implementation details
    for {
      name <- ZIO.fromEither(Name.from(request.getAccountInfo.getName)).mapError(error => Status.INVALID_ARGUMENT.withDescription(error))
      account <- accountsRepo.insert(AccountInfoDto(
        name = name,
        passwordHash = request.password //TODO password hashing
      )).mapError(e => Status.INTERNAL.withDescription(e.getMessage)).debug("pls work?")
      accessToken <- authorizer.createAccessToken(account.id).mapError(e => Status.INTERNAL.withDescription(e.getMessage))
      _ <- ZIO.logInfo(s"Account created. Data(accountId: ${account.id})") //TODO create a wrapper for this data
    } yield SignupResponse(account = Option(toProto(account)), accessToken = accessToken)
  }

  override def login(request: LoginRequest): ZIO[Any, Status, LoginResponse] = ???

  override def getAccount(request: GetAccountRequest): ZIO[AuthContext, Status, GetAccountResponse] = for {
    accountId <- authorizer.getAccountId().mapError(_ => Status.UNAUTHENTICATED.withDescription("Invalid or missing access token"))
    maybeAccount <- accountsRepo.get(accountId).mapError(_ => Status.INTERNAL)
    account <- ZIO.fromOption(maybeAccount).mapError(_ => Status.UNAUTHENTICATED.withDescription("Invalid or missing access token"))
    _ <- ZIO.logInfo(s"Account retrieved. Data(accountId: ${account.id})")
  } yield GetAccountResponse(account = Option(toProto(account)))

  //TODO move to separate mapper (maybe even automapper)
  private def toProto(accDto: AccountDto): Account = {
    new Account(
      id = Option(accDto.id.toString),
      dateCreated = Option(toTimestamp(accDto.dateCreated)),
      dateUpdated = Option(toTimestamp(accDto.dateUpdated)),
      revision = Option(accDto.revision),
      information = Option(AccountInfo(name = Option(accDto.accountInfo.name.toString()))),
    )
  }
}

object AccountsService {
  val live = ZLayer.fromFunction(
    (accountsRepo: AccountsRepository, authorizer: Authorizer) =>
      AccountsService(accountsRepo, authorizer).transformContextZIO(AuthContextTransformer(authorizer))
  )
}
