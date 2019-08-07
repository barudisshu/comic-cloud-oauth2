package io.comiccloud.service.resources

import io.comiccloud.entity.EntityCommand

trait ResourceCommand extends EntityCommand

case class CredentialsDeliverCommand(vo: CredentialsFO) extends ResourceCommand {
  override def entityId: String = vo.id
}

case class FindResourceRelateTokenIdCommand(token: String) extends ResourceCommand {
  override def entityId: String = token
}
case class FindResourceRelateAccountIdCommand(accountId: String) extends ResourceCommand {
  override def entityId: String = accountId
}

case class FindResourceRelateClientIdCommand(clientId: String) extends ResourceCommand {
  override def entityId: String = clientId
}
