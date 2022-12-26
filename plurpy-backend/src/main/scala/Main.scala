package org.tomasmo.plurpy

import model.AuthContext
import service.UnauthenticatedException
import v1.account.AccountsService.ZioAccountsService.ZAccountsService
import model.Configs.AuthorizerConfig
import utils.DefaultTimeProvider
import persistence.AccountsRepositoryImpl
import service.Authorizer
import api.AccountsServiceImpl

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.grpc.{ServerBuilder, Status}
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{RequestContext, Server, ServerLayer}
import zio.Console.printLine
import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource
import zio.logging.backend.SLF4J

object Main extends zio.ZIOAppDefault {

  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  def port: Int = 9000

  def welcome: ZIO[Any, Throwable, Unit] =
    printLine("Server is running. Press Ctrl-C to stop.")

  val configLayer: ZLayer[Any, ReadError[String], AuthorizerConfig] =
    ZLayer {
      read {
        descriptor[AuthorizerConfig].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$("AuthorizerConfig"))
        )
      }
    }

  val AuthorizationTokenKey = io.grpc.Metadata.Key.of("authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER)

  def getAuthContext(
      auth: Authorizer,
      bearerToken: String
  ): UIO[AuthContext] = {
    auth
      .getAuthContext(bearerToken)
  }

  val timeProviderLayer = ZLayer.succeed(new DefaultTimeProvider)

  val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
  val dsLayer = Quill.DataSource.fromPrefix("myDatabaseConfig")

  val accountsRepositoryLayer = ZLayer.fromFunction(AccountsRepositoryImpl(_, _))

  val authorizerLayer = ZLayer.fromFunction(Authorizer(_, _))

  val foo: ZLayer[
    AccountsRepositoryImpl with Authorizer,
    Nothing,
    ZAccountsService[Any, RequestContext]
  ] = ZLayer.fromFunction(
    (accountRepoImpl: AccountsRepositoryImpl, auth: Authorizer) =>
      AccountsServiceImpl(accountRepoImpl, auth)
        .transformContextZIO { (rc: RequestContext) =>
          rc.metadata
            .get(AuthorizationTokenKey).someOrElse("Bearer EMPTY") //TODO fix this hack
            .flatMap(authorization => getAuthContext(auth, authorization.split(" ")(1))) //TODO not sure this should be here also validation if that is a bearer token would be nice
        }
  )
  val accountsServiceLayer = ZLayer.make[ZAccountsService[Any, RequestContext]](
    foo,
    accountsRepositoryLayer,
    quillLayer,
    dsLayer,
    authorizerLayer,
    timeProviderLayer,
    configLayer,
  )

  def serverLive = ServerLayer.fromServiceLayer(ServerBuilder.forPort(port))(
    accountsServiceLayer
  )

  val services = welcome *> serverLive.build *> ZIO.never

  def run = services
}
