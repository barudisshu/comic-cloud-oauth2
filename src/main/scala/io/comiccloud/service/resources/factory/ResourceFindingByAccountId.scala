package io.comiccloud.service.resources.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.comiccloud.modeling.database.AccountDatabase
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.resources.FindResourceRelateAccountIdCommand

object ResourceFindingByAccountId {
  def props(): Props = Props(new ResourceFindingByAccountId())
}

class ResourceFindingByAccountId() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindResourceRelateAccountIdCommand(accountId) =>
      context become resolveFindingAccountById(sender)
      AccountDatabase.AccountModel.getByAccountId(UUID.fromString(accountId)) pipeTo self
  }

  def resolveFindingAccountById(replyTo: ActorRef): Receive = {
    case Some(account) =>
      replyTo ! FullResult(account)
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
