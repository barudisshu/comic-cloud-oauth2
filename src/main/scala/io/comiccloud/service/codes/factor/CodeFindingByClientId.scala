package io.comiccloud.service.codes.factor

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.codes.request.FindCodeRelateClientIdReq

object CodeFindingByClientId {
  def props(clientRepo: ClientsRepository): Props = Props(new CodeFindingByClientId(clientRepo))
}

class CodeFindingByClientId(clientRepo: ClientsRepository) extends Actor with ActorLogging with CommonBehaviorResolver {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindCodeRelateClientIdReq(clientId) =>
      context become resolveFindingClientById(sender)
      clientRepo.find(clientId) pipeTo self
  }
}
