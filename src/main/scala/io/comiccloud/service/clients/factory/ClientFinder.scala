package io.comiccloud.service.clients.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.AccountDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.clients.FindClientByAccountIdCommand

private[clients] object ClientFinder {
  def props(): Props = Props(new ClientFinder())
}

private[clients] class ClientFinder() extends Actor with ActorLogging with CommonBehaviorResolver {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindClientByAccountIdCommand(accountId) =>
      context become resolveFindingAccountById(sender)
      AccountDatabase.AccountModel.getByAccountId(UUID.fromString(accountId)) pipeTo self
  }
}
