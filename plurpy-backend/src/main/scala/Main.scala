package org.tomasmo.plurpy

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.grpc.ServerBuilder
import org.tomasmo.plurpy.api.{AccountsService, RoomsService}
import org.tomasmo.plurpy.model.Configs.AuthorizerConfig
import org.tomasmo.plurpy.persistence.AccountsRepository
import org.tomasmo.plurpy.service.Authorizer
import org.tomasmo.plurpy.utils.TimeProvider
import org.tomasmo.plurpy.v1.account.AccountsService.ZioAccountsService.ZAccountsService
import scalapb.zio_grpc.{RequestContext, ServerLayer}
import zio.Console.printLine
import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource
import zio.logging.backend.SLF4J

object Main extends zio.ZIOAppDefault {

  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  //TODO also move to config
  def port: Int = 9000

  def run = server

  val server = welcome *> grpcServices.build *> ZIO.never

  def welcome: ZIO[Any, Throwable, Unit] =
    printLine("Server is running. Press Ctrl-C to stop.")

  def grpcServices = {
    ServerLayer.fromServiceLayer(ServerBuilder.forPort(port))(accountsServiceLayer) ++
      ServerLayer.fromServiceLayer(ServerBuilder.forPort(port + 1))(RoomsService.live)
  }

  val configLayer: ZLayer[Any, ReadError[String], AuthorizerConfig] =
    ZLayer {
      read {
        descriptor[AuthorizerConfig].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$("AuthorizerConfig"))
        )
      }
    }

  val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
  val dsLayer = Quill.DataSource.fromPrefix("myDatabaseConfig")

  val accountsServiceLayer = ZLayer.make[ZAccountsService[Any, RequestContext]](
    AccountsService.live,
    AccountsRepository.live,
    quillLayer,
    dsLayer,
    Authorizer.live,
    TimeProvider.live,
    configLayer,
  )
}
