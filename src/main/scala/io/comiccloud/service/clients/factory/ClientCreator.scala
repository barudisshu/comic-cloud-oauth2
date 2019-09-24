package io.comiccloud.service.clients.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.models.Client
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.clients.request.CreateClientReq

private[clients] object ClientCreator {
  def props(clientRepo: ClientsRepository): Props = Props(new ClientCreator(clientRepo))
}

class ClientCreator(clientRepo: ClientsRepository) extends Actor with ActorLogging with CommonBehaviorResolver {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case CreateClientReq(vo) =>
      val client = Client(
        uid = vo.id,
        ownerId = vo.ownerId,
        clientId = vo.clientId,
        clientSecret = vo.clientSecret,
        redirectUri = vo.redirectUri,
        grantType = vo.grantType,
        createdAt = vo.createdAt
      )
      context become feedback[Client](vo, sender)
      clientRepo.insert(client) pipeTo self
  }

}
