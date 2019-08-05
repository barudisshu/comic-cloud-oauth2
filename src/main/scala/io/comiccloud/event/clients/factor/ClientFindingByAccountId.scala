package io.comiccloud.event.clients.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.accounts.AccountInfo
import io.comiccloud.event.clients.FindClientByAccountIdCommand
import io.comiccloud.models.Account
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest.{EmptyResult, FullResult}

private[clients] object ClientFindingByAccountId {
  def props(clientsRepo: ClientsRepository, accountsRepo: AccountsRepository): Props =
    Props(new ClientFindingByAccountId(clientsRepo, accountsRepo))
}

private[clients] class ClientFindingByAccountId(clientsRepo: ClientsRepository,
                                                accountsRepo: AccountsRepository) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindClientByAccountIdCommand(accountId) =>
      context become findByAccountUid(sender)
      accountsRepo.findByUid(accountId) pipeTo self
  }
  def findByAccountUid(replyTo: ActorRef): Receive = {
    case Some(account: Account) =>
      val accountFO = AccountInfo(
        id = account.uid,
        username = account.username,
        password = account.password,
        salt = account.salt,
        email = account.email,
        phone = account.phone,
        createdAt = account.createdAt)
      replyTo ! FullResult(accountFO)
      self ! PoisonPill

    case f: akka.actor.Status.Failure =>
      replyTo ! EmptyResult
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self
  }
}
