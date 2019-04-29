package io.comiccloud.event.clients.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.clients.{ClientFO, FindClientByAccountIdCommand}
import io.comiccloud.models.Client
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.rest.{EmptyResult, FullResult}

private[clients] object ClientFindingByAccountId {
  def props(repo: ClientsRepository): Props = Props(new ClientFindingByAccountId(repo))
}

private[clients] class ClientFindingByAccountId(repo: ClientsRepository) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindClientByAccountIdCommand(accountId) =>
      context become findByAccountUid(sender)
      repo.findByAccountUid(accountId) pipeTo self
  }

  def findByAccountUid(replyTo: ActorRef): Receive = {
    case clients: Seq[Client @unchecked] =>
      val clientFOs = clients.map(client =>
        ClientFO(
           id = client.clientId,
          ownerId = client.clientId,
          clientId = client.clientId,
          clientSecret = client.clientSecret,
          redirectUri = client.redirectUri,
          createdAt = client.createdAt
        )
      )
      replyTo ! FullResult(clientFOs)
      self ! PoisonPill

    case f: akka.actor.Status.Failure =>
      replyTo ! EmptyResult
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self

  }
}
