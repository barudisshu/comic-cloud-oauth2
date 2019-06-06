package io.comiccloud.event.tokens.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.clients.ClientFO
import io.comiccloud.event.tokens.FindTokenRelateClientIdCommand
import io.comiccloud.models.Client
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.rest.{EmptyResult, FullResult}

private[tokens] object TokenFindingByClientId {
  def props(repo: ClientsRepository): Props = Props(new TokenFindingByClientId(repo))
}

class TokenFindingByClientId(repo: ClientsRepository) extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case FindTokenRelateClientIdCommand(clientId) =>
      context become findByClientId(sender)
      repo.findByClientId(clientId) pipeTo self
  }

  def findByClientId(replyTo: ActorRef): Receive = {
    case Some(client: Client) =>
      val clientFO = ClientFO(
        id = client.uid,
        ownerId = client.ownerId,
        clientId = client.clientId,
        clientSecret = client.clientSecret,
        client.redirectUri
      )
      replyTo ! FullResult(clientFO)
      self ! PoisonPill
    case f: akka.actor.Status.Failure =>
      replyTo ! EmptyResult
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self
  }
}
