package org.tomasmo.plurpy.domain

import domain.CommonTypes.{AccountId, Name}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.Instant

case class Account(
    id: AccountId,
    passwordHash: String,
    dateCreated: Instant,
    dateUpdated: Instant,
    revision: Int,
    accountInfo: AccountInfo,
)

case class AccountInfo(
    name: Name,
)

object Account {
  implicit val encoder: JsonEncoder[Account] = DeriveJsonEncoder.gen[Account]
  implicit val decoder: JsonDecoder[Account] = DeriveJsonDecoder.gen[Account]
}

object AccountInfo {
  // WTF is this code gymnastics? Why doesn't it find the name type implicits without this?
  private implicit val nameEncoder = Name.encoder
  private implicit val nameDecoder = Name.decoder


  implicit val encoder: JsonEncoder[AccountInfo] = DeriveJsonEncoder.gen[AccountInfo]
  implicit val decoder: JsonDecoder[AccountInfo] = DeriveJsonDecoder.gen[AccountInfo]
}