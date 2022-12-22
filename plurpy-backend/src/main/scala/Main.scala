package org.tomasmo.plurpy

import model.Configs.AuthorizerConfig
import utils.DefaultTimeProvider
import persistence.AccountsRepositoryImpl
import service.Authorizer
import api.AccountsServiceImpl

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{Server, ServerLayer}
import zio.Console.printLine
import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource
import zio.logging.backend.SLF4J

object Main extends zio.ZIOAppDefault {

  override val bootstrap = SLF4J.slf4j

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

  val timeProviderLayer = ZLayer.succeed(new DefaultTimeProvider)

  val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
  val dsLayer = Quill.DataSource.fromPrefix("myDatabaseConfig")

  val accountsRepositoryLayer = ZLayer.fromFunction(AccountsRepositoryImpl(_, _))

  val authorizerLayer = ZLayer.fromFunction(Authorizer(_, _))

  val accountsServiceLayer = ZLayer.make[AccountsServiceImpl](
    ZLayer.fromFunction(AccountsServiceImpl(_, _)),
    accountsRepositoryLayer,
    quillLayer,
    dsLayer,
    authorizerLayer,
    timeProviderLayer,
    configLayer,
  )

  def builder = ServerBuilder.forPort(port).addService(ProtoReflectionService.newInstance())

  def serverLive: ZLayer[Any, Throwable, Server] = ServerLayer.fromServiceLayer(builder)(accountsServiceLayer)

  val services = welcome *> serverLive.build *> ZIO.never

  def run = services
}