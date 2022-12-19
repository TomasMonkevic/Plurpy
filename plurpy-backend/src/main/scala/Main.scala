package org.tomasmo.plurpy

import scalapb.zio_grpc.CanBind.canBindAny
import zio._
import zio.Console._
import scalapb.zio_grpc.{Server, ServerLayer, ServerMain, ServiceList}

object Main extends ServerMain {
  def services: ServiceList[Any] = ServiceList.add(AccountsServiceImpl)
}