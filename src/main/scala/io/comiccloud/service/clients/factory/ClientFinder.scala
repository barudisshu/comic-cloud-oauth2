package io.comiccloud.service.clients.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.clients.request.{FindClientByAccountIdReq, FindClientByIdReq}

private[clients] object ClientFinder {
  def props(clientRepo: ClientsRepository, accountRepo: AccountsRepository): Props =
    Props(new ClientFinder(clientRepo, accountRepo))
}

private[clients] class ClientFinder(clientRepo: ClientsRepository, accountRepo: AccountsRepository)
    extends Actor
    with ActorLogging
    with CommonBehaviorResolver {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindClientByAccountIdReq(accountId) =>
      context become resolveFindingAccountById(sender)
      accountRepo.findByUid(accountId) pipeTo self

    case FindClientByIdReq(clientId) =>
      context become resolveFindingClientById(sender)
      clientRepo.findByClientId(clientId) pipeTo self
  }
}
