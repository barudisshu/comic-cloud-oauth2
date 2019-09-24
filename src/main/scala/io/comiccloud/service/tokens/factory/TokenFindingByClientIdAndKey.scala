package io.comiccloud.service.tokens.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.request.FindTokenRelateClientReq

private[tokens] object TokenFindingByClientIdAndKey {
  def props(clientRepo: ClientsRepository): Props = Props(new TokenFindingByClientIdAndKey(clientRepo))
}

class TokenFindingByClientIdAndKey(clientRepo: ClientsRepository) extends Actor with ActorLogging with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindTokenRelateClientReq(clientId, clientSecret) =>
      context become resolveFindingClientById(sender)
      clientRepo.findByClientIdAndClientSecret(clientId, clientSecret) pipeTo self
  }
}
