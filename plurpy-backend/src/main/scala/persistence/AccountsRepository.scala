package org.tomasmo.plurpy.persistence

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import org.tomasmo.plurpy.model.{Account, AccountInfo}
import org.tomasmo.plurpy.utils.TimeProvider
import zio._

import java.sql.SQLException
import java.util.UUID

trait AccountsRepository {
  def insert(accountInfo: AccountInfo): IO[SQLException, Account]

  //TODO make specific uuid (refined types)
  def get(accountId: UUID): IO[SQLException, Option[Account]]
}

final case class AccountsRepositoryImpl(
    quill: Quill.Postgres[SnakeCase],
    timeProvider: TimeProvider
) extends AccountsRepository {

  import quill._

  def insert(accountInfo: AccountInfo): IO[SQLException, Account] = {
    val now = timeProvider.now()

    val insertAccountQuery = quote {
      query[Account].insertValue(lift(
        Account(id = UUID.randomUUID(),
          dateCreated = now,
          dateUpdated = now,
          revision = 1,
          accountInfo = accountInfo
        )
      )).returning(account => account)
    }

    run(insertAccountQuery)
  }

  def get(accountId: UUID): IO[SQLException, Option[Account]] = {
    def getAccountByIdQuery(id: UUID) = quote {
      query[Account].filter(account => account.id == lift(id))
    }

    run(getAccountByIdQuery(accountId)).map(r => r.headOption)
  }
}

object AccountsRepository {
  val live = ZLayer.fromFunction(AccountsRepositoryImpl(_, _))
}
