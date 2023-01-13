package org.tomasmo.plurpy.domain

import java.time.Instant
import java.util.UUID

case class Account(
    id: UUID,
    dateCreated: Instant,
    dateUpdated: Instant,
    revision: Int,
    accountInfo: AccountInfo
)

case class AccountInfo(
    name: String,
    passwordHash: String,
)