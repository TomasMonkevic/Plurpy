package domain

import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.string._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import scala.language.implicitConversions
import java.util.UUID

object CommonTypes {
  @newtype case class AccountId(id: UUID)

  object AccountId {
    def random = AccountId(UUID.randomUUID())

    implicit val encoder: JsonEncoder[AccountId] = JsonEncoder[UUID].contramap(_.id)
    implicit val decoder: JsonDecoder[AccountId] = JsonDecoder[UUID].map(AccountId(_))
  }

  type Name = String Refined MatchesRegex["(?U)\\w{3,25}"]
  object Name extends RefinedTypeOps[Name, String] {
    implicit val encoder: JsonEncoder[Name] = JsonEncoder[String].contramap(_.toString)
    implicit val decoder: JsonDecoder[Name] = JsonDecoder[String].mapOrFail(Name.from(_))
  }
}
