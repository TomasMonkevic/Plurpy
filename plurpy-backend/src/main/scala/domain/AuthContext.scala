package org.tomasmo.plurpy.domain

import java.util.UUID

case class AuthContext(accountId: Option[UUID])

object AuthContext {
  val empty = AuthContext(
    accountId = None
  )
}
