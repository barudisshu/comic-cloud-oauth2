package io.comiccloud.service.auths

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.comiccloud.event.auths._

object AuthAssociate {
  val Name = "auth-associate"
  def props(): Props = Props(new AuthAssociate())
}

class AuthAssociate() extends Actor with ActorLogging {
  val auth: ActorRef = context.actorOf(AuthEntity.props())
  override def receive: Receive = {
    case command: VerificationAuthCommand =>
      auth.forward(command)
  }
}
