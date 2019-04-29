package io.comiccloud.event.codes

import io.comiccloud.entity.EntityCommand

trait CodeCommand extends EntityCommand

case class CreateCodeCommand(vo: CodeFO) extends CodeCommand {
  override def entityId: String = vo.id
}

case class FindCodeByIdCommand(id: String) extends CodeCommand {
  override def entityId: String = id
}

case class FindCodeByAccountIdCommand(accountId: String) extends CodeCommand {
  override def entityId: String = accountId
}