package io.comiccloud.service.tokens.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.ClientDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.FindTokenRelateClientCommand

private[tokens] object TokenFindingByClient {
  def props(): Props = Props(new TokenFindingByClient())
}

class TokenFindingByClient() extends Actor with ActorLogging with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case FindTokenRelateClientCommand(clientId, clientSecret) =>
      context become resolveFindingClientById(sender)
      ClientDatabase.ClientModel.getByClientIdAndKey(UUID.fromString(clientId), UUID.fromString(clientSecret)) pipeTo self
  }
}
