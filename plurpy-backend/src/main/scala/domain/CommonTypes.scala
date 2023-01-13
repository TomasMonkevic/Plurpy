package domain

import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.string._

import scala.language.implicitConversions
import java.util.UUID

package object CommonTypes {
  @newtype case class AccountId(id: UUID)

  type Name = String Refined MatchesRegex["[\\w]{3,25}"] //TODO would like to accepts unicode values too
  object Name extends RefinedTypeOps[Name, String]

  type Email = String Refined MatchesRegex["(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"]
  object Email extends RefinedTypeOps[Email, String]
}
