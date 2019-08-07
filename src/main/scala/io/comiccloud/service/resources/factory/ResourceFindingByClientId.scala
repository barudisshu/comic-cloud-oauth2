package io.comiccloud.service.resources.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.comiccloud.modeling.database.ClientDatabase
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.resources.FindResourceRelateClientIdCommand

object ResourceFindingByClientId {
  def props(): Props = Props(new ResourceFindingByClientId())
}

class ResourceFindingByClientId() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindResourceRelateClientIdCommand(clientId) =>
      context become resolveFindingClientById(sender)
      ClientDatabase.ClientModel.getByClientId(UUID.fromString(clientId)) pipeTo self
  }

  def resolveFindingClientById(replyTo: ActorRef): Receive = {
    case Some(client) =>
      replyTo ! FullResult(client)
      context stop self
    case f: akka.actor.Status.Failure =>
      log.debug("{}", f.cause.getMessage)
      replyTo ! EmptyResult
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self

  }
}
