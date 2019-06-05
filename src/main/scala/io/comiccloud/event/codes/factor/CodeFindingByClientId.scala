package io.comiccloud.event.codes.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.clients.ClientFO
import io.comiccloud.event.codes.FindCodeByClientIdCommand
import io.comiccloud.models.Client
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.rest.{EmptyResult, FullResult}

object CodeFindingByClientId {
  def props(clientsRepo: ClientsRepository): Props = Props(new CodeFindingByClientId(clientsRepo))
}

class CodeFindingByClientId(clientsRepo: ClientsRepository) extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindCodeByClientIdCommand(clientId) =>
      context become findByClientUid(sender)
      clientsRepo.findByUid(clientId) pipeTo self
  }
  def findByClientUid(replyTo: ActorRef): Receive = {
    case Some(client: Client) =>
      val clientFO = ClientFO(
        id = client.clientId,
        ownerId = client.ownerId,
        clientId = client.clientId,
        clientSecret = client.clientSecret,
        redirectUri = client.redirectUri,
        createdAt = client.createdAt
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
