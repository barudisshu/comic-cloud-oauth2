package io.comiccloud.event.clients

import io.comiccloud.entity.EntityCommand

trait ClientCommand extends EntityCommand

case class CreateClientCommand(vo: ClientFO) extends ClientCommand {
  override def entityId: String = vo.id
}

case class FindClientByIdCommand(id: String) extends ClientCommand {
  override def entityId: String = id
}

case class FindClientByAccountIdCommand(accountId: String) extends ClientCommand {
  override def entityId: String = accountId
}
