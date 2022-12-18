package org.tomasmo.plurpy

import scalapb.zio_grpc.CanBind.canBindAny
import zio._
import zio.Console._
import scalapb.zio_grpc.{Server, ServerLayer, ServerMain, ServiceList}

//object Main extends ZIOAppDefault {
//
//  def run = myAppLogic
//
//  val myAppLogic =
//    for {
//      _    <- printLine("Hello! What is your name?")
//      name <- readLine
//      _    <- printLine(s"Hello, ${name}, welcome to ZIO!")
//    } yield ()
//}
object Main extends ServerMain {
  def services: ServiceList[Any] = ServiceList.add(AccountsServiceImpl)
}