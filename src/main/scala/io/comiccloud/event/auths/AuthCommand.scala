package io.comiccloud.event.auths

import io.comiccloud.entity.EntityCommand

trait AuthCommand extends EntityCommand

case class VerificationAuthCommand(vo: AuthFO) extends AuthCommand {
  override def entityId: String = vo.id
}
case class VerifiedAuthCommand(vo: AuthFO) extends AuthCommand {
  override def entityId: String = vo.id
}
