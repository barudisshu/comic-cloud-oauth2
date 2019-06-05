package io.comiccloud.event.clients.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.clients.{ClientFO, CreateClientCommand}
import io.comiccloud.models.Client
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.rest.{EmptyResult, ErrorMessage, Failure, FailureType, FullResult}

private[clients] object ClientCreator {
  def props(repo: ClientsRepository): Props = Props(new ClientCreator(repo))
}

class ClientCreator(repo: ClientsRepository) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case CreateClientCommand(vo) =>
      val client = Client(
        id = None,
        uid = vo.id,
        ownerId = vo.ownerId,
        clientId = vo.clientId,
        clientSecret = vo.clientSecret,
        redirectUri = vo.redirectUri,
        createdAt = vo.createdAt
      )
      context become feedback(vo, sender)
      repo.insert(client) pipeTo self
  }

  def feedback(o: ClientFO, replyTo: ActorRef): Receive = {
    case cli: Client =>
      replyTo ! FullResult(o)
      self ! PoisonPill
    case f: akka.actor.Status.Failure =>
      replyTo ! Failure(FailureType.Service, ErrorMessage("500", Some(s"db exception: ${f.cause.getLocalizedMessage}")))
      self ! PoisonPill
    case _ =>
      replyTo ! EmptyResult
      context stop self
  }
}
