package io.comiccloud.service.resources.factory

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.modeling.database.TokenDatabase
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.resources.FindResourceRelateTokenIdCommand

object ResourceFindingByToken {
  def props(): Props = Props(new ResourceFindingByToken())
}

class ResourceFindingByToken() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindResourceRelateTokenIdCommand(token) =>
      context become resolveFindingTokenById(sender)
      TokenDatabase.TokenByAccessTokenModel.getByAccessToken(token) pipeTo self
  }

  def resolveFindingTokenById(replyTo: ActorRef): Receive = {
    case Some(token) =>
      replyTo ! FullResult(token)
      self ! PoisonPill
    case f: akka.actor.Status.Failure =>
      log.debug("{}", f.cause.getMessage)
      replyTo ! EmptyResult
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self
  }
}
