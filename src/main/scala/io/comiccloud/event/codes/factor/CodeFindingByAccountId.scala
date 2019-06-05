package io.comiccloud.event.codes.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.accounts.AccountFO
import io.comiccloud.event.codes.FindCodeByAccountIdCommand
import io.comiccloud.models.Account
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.rest.{EmptyResult, FullResult}

object CodeFindingByAccountId {
  def props(accountsRepo: AccountsRepository): Props = Props(new CodeFindingByAccountId(accountsRepo))
}

class CodeFindingByAccountId(accountsRepo: AccountsRepository) extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {

    case FindCodeByAccountIdCommand(accountId) =>
      context become findByAccountUid(sender)
      accountsRepo.findByUid(accountId) pipeTo self
  }
  def findByAccountUid(replyTo: ActorRef): Receive = {
    case Some(account: Account) =>
      val accountFO = AccountFO(
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
