package io.comiccloud.event.resources

import io.comiccloud.entity.EntityCommand

trait ResourceCommand extends EntityCommand

case class CredentialVerifiedCommand(vo: ResourceFO) extends ResourceCommand {
  override def entityId: String = vo.id
}
