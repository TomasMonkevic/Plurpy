package org.tomasmo.plurpy

import persistence.AccountsRepositoryImpl
import service.AccountsServiceImpl

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import scalapb.zio_grpc.{Server, ServerLayer}
import zio.Console.printLine
import zio._

object Main extends zio.ZIOAppDefault {
  def port: Int = 9000

  def welcome: ZIO[Any, Throwable, Unit] =
    printLine("Server is running. Press Ctrl-C to stop.")

  val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
  val dsLayer = Quill.DataSource.fromPrefix("myDatabaseConfig")

  val accountsRepositoryLayer = ZLayer.fromFunction(AccountsRepositoryImpl(_))

  val accountsServiceLayer = ZLayer.make[AccountsServiceImpl](
    ZLayer.fromFunction(AccountsServiceImpl(_)),
    accountsRepositoryLayer,
    quillLayer,
    dsLayer,
  )

  def builder = ServerBuilder.forPort(port).addService(ProtoReflectionService.newInstance())

  def serverLive: ZLayer[Any, Throwable, Server] = ServerLayer.fromServiceLayer(builder)(accountsServiceLayer)

  val services = welcome *> serverLive.build *> ZIO.never

  def run = services
}