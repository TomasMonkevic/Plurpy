package org.tomasmo.plurpy
package model

import java.util.UUID

case class AuthContext(accountId: Option[UUID])

object AuthContext {
  val empty = AuthContext(
    accountId = None
  )
}
