package io.comiccloud.service.accounts

import io.comiccloud.entity.EntityCommand

trait AccountCommand extends EntityCommand

case class CreateAccountCommand(vo: AccountFO) extends AccountCommand {
  override def entityId: String = vo.id
}

case class FindAccountByIdCommand(id: String) extends AccountCommand {
  override def entityId: String = id
}

case class FindAccountByUsernameCommand(id: String, username: String) extends AccountCommand {
  override def entityId: String = id
}