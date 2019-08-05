package io.comiccloud.service.clients.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.modeling.database.AccountDatabase
import io.comiccloud.modeling.entity.Account
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.accounts.AccountFO
import io.comiccloud.service.clients.FindClientByAccountIdCommand

private[clients] object ClientFindingByAccountId {
  def props(): Props = Props(new ClientFindingByAccountId())
}

private[clients] class ClientFindingByAccountId() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindClientByAccountIdCommand(accountId) =>
      context become findByAccountUid(sender)
      AccountDatabase.AccountModel.getByAccountId(UUID.fromString(accountId)) pipeTo self
  }
  def findByAccountUid(replyTo: ActorRef): Receive = {
    case Some(account: Account) =>
      val accountFO = AccountFO(
        id = account.id.toString,
        username = account.username,
        password = account.password,
        salt = account.salt,
        email = account.email,
        phone = account.phone,
        createdAt = account.created_at
      )
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
