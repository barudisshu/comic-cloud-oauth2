package io.comiccloud.event.resources

import io.comiccloud.entity.EntityCommand

trait ResourceCommand extends EntityCommand

case class CredentialsDeliverCommand(vo: CredentialsFO) extends ResourceCommand {
  override def entityId: String = vo.id
}

case class CredentialsVerifiedCommand(vo: ResourceFO) extends ResourceCommand {
  override def entityId: String = vo.id
}
