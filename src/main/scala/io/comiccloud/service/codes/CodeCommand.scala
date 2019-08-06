package io.comiccloud.service.codes

import io.comiccloud.entity.EntityCommand

trait CodeCommand extends EntityCommand

case class CreateCodeCommand(vo: CodeFO) extends CodeCommand {
  override def entityId: String = vo.id
}

case class FindCodeRelateAccountIdCommand(accountId: String) extends CodeCommand {
  override def entityId: String = accountId
}

case class FindCodeRelateClientIdCommand(clientId: String) extends CodeCommand {
  override def entityId: String = clientId
}