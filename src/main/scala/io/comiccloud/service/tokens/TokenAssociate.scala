package io.comiccloud.service.tokens

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate

object TokenAssociate {
  val Name           = "token-associate"
  def props(): Props = Props(new TokenAssociate())
}

class TokenAssociate() extends Aggregate[TokenState, TokenEntity] {

  override def entityProps: Props = TokenEntity.props()

  override def receive: Receive = {
    case command: CreateClientCredentialsTokenCommand =>
      forwardCommand(command)
  }
}
