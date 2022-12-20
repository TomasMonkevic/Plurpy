package org.tomasmo.plurpy
package persistence

import model.{Account, AccountInfo}

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio._

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

trait AccountsRepository[F[_]] {
  def insert(accountInfo: AccountInfo): F[Account]

  //TODO make specific uuid
  def get(accountId: UUID): F[Option[Account]]
}

//TODO remove later
object Bar {
  type Foo[_] = ZIO[Any, SQLException, _]
}

final case class AccountsRepositoryImpl(quill: Quill.Postgres[SnakeCase]) extends AccountsRepository[Bar.Foo] {

  import quill._

  def insert(accountInfo: AccountInfo): ZIO[Any, SQLException, Account] = {
    //TODO make time provider
    val now = Instant.now()

    val insertAccountQuery = quote {
      query[Account].insertValue(lift(
        Account(id = UUID.randomUUID(),
          dateCreated = now,
          dateUpdated = now,
          revision = 1,
          accountInfo = accountInfo
        )
      )).returning(account => account) //TODO maybe returningGenerated??
    }

    run(insertAccountQuery)
  }

  def get(accountId: UUID): ZIO[Any, SQLException, Option[Account]] = {
    def getAccountByIdQuery(id: UUID) = quote {
      query[Account].filter(account => account.id == lift(id))
    }

    run(getAccountByIdQuery(accountId)).map(r => r.headOption)
  }
}
