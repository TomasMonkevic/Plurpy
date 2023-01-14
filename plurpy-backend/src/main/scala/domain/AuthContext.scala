package org.tomasmo.plurpy.domain

import domain.CommonTypes.AccountId
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class AuthContext(accountId: Option[AccountId])

object AuthContext {
  val empty = AuthContext(
    accountId = None
  )

  implicit val encoder: JsonEncoder[AuthContext] = DeriveJsonEncoder.gen[AuthContext]
  implicit val decoder: JsonDecoder[AuthContext] = DeriveJsonDecoder.gen[AuthContext]
}
