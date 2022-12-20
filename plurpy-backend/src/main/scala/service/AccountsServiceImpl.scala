package org.tomasmo.plurpy
package service

import persistence.AccountsRepositoryImpl
import v1.account.AccountsService.ZioAccountsService.ZAccountsService
import v1.account.AccountsService._

import io.grpc.Status
import zio.Console.printLine
import zio.ZIO

//TODO later don't depend on specific effect type. Use AccountsRepository
final case class AccountsServiceImpl(accountsRepo: AccountsRepositoryImpl) extends ZAccountsService[Any, Any] {
  override def signup(request: SignupRequest): ZIO[Any, Status, SignupResponse] = {
    printLine(s"sdsdf request: $request").orDie zipRight
      ZIO.succeed(SignupResponse(authToken = s"Very secret token for, ${request.getAccount.name}"))
  }

  override def login(request: LoginRequest): ZIO[Any, Status, LoginResponse] = ???

  override def logout(request: LogoutRequest): ZIO[Any, Status, LogoutResponse] = ???
}
