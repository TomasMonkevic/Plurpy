package org.tomasmo.plurpy.repository

import domain.CommonTypes.AccountId
import io.getquill.{JsonValue, MappedEncoding, SnakeCase}
import io.getquill.jdbczio.Quill
import org.tomasmo.plurpy.domain.{Account, AccountInfo}
import org.tomasmo.plurpy.utils.TimeProvider
import zio._
import zio.json._

import java.sql.SQLException
import java.util.UUID

trait AccountsRepository {
  def insert(accountInfo: AccountInfo, passwordHash: String): IO[SQLException, Account]

  def get(accountId: AccountId): IO[SQLException, Option[Account]]
}

case class AccountsRepositoryImpl(
    quill: Quill.Postgres[SnakeCase],
    timeProvider: TimeProvider
) extends AccountsRepository {

  implicit val encodeAccountInfo = MappedEncoding[AccountInfo, JsonValue[AccountInfo]](JsonValue[AccountInfo](_))
  implicit val decodeAccountInfo = MappedEncoding[JsonValue[AccountInfo], AccountInfo](_.value)

  implicit val encodeAccountId = MappedEncoding[AccountId, UUID](_.id)
  implicit val decodeAccountId = MappedEncoding[UUID, AccountId](AccountId(_))

  import quill._

  def insert(accountInfo: AccountInfo, passwordHash: String): IO[SQLException, Account] = {
    val now = timeProvider.now()

    val insertAccountQuery = quote {
      query[Account].insertValue(lift(
        Account(id = AccountId.random,
          passwordHash = passwordHash,
          dateCreated = now,
          dateUpdated = now,
          revision = 1,
          accountInfo = accountInfo
        )
      )).returning(account => account)
    }

    run(insertAccountQuery)
  }

  def get(accountId: AccountId): IO[SQLException, Option[Account]] = {
    def getAccountByIdQuery(id: AccountId) = quote {
      query[Account].filter(account => account.id == lift(id))
    }

    run(getAccountByIdQuery(accountId)).map(r => r.headOption)
  }
}

object AccountsRepository {
  val live = ZLayer.fromFunction(AccountsRepositoryImpl(_, _))
}
