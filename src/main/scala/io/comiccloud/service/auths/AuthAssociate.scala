package io.comiccloud.service.auths

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.auths._

object AuthAssociate {
  val Name = "auth-associate"
  def props(): Props = Props(new AuthAssociate())
}

class AuthAssociate() extends Aggregate[AuthState, AuthEntity] {
  override def entityProps: Props = AuthEntity.props()
  override def receive: Receive = {
    case command: VerificationAuthCommand =>
      forwardCommand(command)
  }
}
