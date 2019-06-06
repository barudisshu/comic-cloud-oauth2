package io.comiccloud.event.tokens.factor

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.event.tokens.CreateValidatedClientCredentialTokenCommand
import io.comiccloud.event.tokens.TokenEntity.CreateValidatedToken
import io.comiccloud.rest.FullResult

private[tokens] object TokenClientCredentialCreator {
  def props(): Props = Props(new TokenClientCredentialCreator())
}

class TokenClientCredentialCreator() extends Actor with ActorLogging {
  override def receive: Receive = {
    case CreateValidatedClientCredentialTokenCommand(vo) =>
      sender ! FullResult(vo)
      context.parent.tell(CreateValidatedToken(vo), sender)
  }
}
