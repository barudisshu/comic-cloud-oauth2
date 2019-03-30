package io.comiccloud.event.accounts

import io.comiccloud.entity.EntityEvent

trait AccountEvent extends EntityEvent { def entityType: String = "account"}

case class AccountCreatedEvent(account: AccountFO) extends AccountEvent