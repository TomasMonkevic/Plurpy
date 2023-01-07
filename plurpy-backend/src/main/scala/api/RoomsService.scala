package org.tomasmo.plurpy.api

import org.tomasmo.plurpy.v1.room.RoomsService.ZioRoomsService.ZRoomsService
import org.tomasmo.plurpy.v1.room.RoomsService._
import io.grpc.Status
import zio.{ZIO, ZLayer}

case class RoomsService() extends ZRoomsService[Any, Any] {
  override def create(request: CreateRequest): ZIO[Any, Status, CreateResponse] = for {
    _ <- ZIO.logInfo("TODO create room request received")
  } yield CreateResponse()
}

object RoomsService {
  val live = ZLayer.succeed(new RoomsService)
}
