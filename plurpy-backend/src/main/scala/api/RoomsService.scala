package org.tomasmo.plurpy
package api

import v1.room.RoomsService.ZioRoomsService.ZRoomsService
import v1.room.RoomsService._

import io.grpc.Status
import zio.ZIO

case class RoomsService() extends ZRoomsService[Any, Any] {
  override def create(request: CreateRequest): ZIO[Any, Status, CreateResponse] = for {
    _ <- ZIO.logInfo("TODO create room request received")
  } yield CreateResponse()
}
