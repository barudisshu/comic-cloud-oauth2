package io.comiccloud.event.accounts.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.accounts.{AccountFO, FindAccountByIdCommand}
import io.comiccloud.models.Account
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.rest.{EmptyResult, FullResult}

object AccountFindingById {
  def props(repo: AccountsRepository): Props = Props(new AccountFindingById(repo))
}

class AccountFindingById(repo: AccountsRepository) extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindAccountByIdCommand(id) =>
      context become findByUid(sender)
      repo.findByUid(id) pipeTo self
  }

  def findByUid(replyTo: ActorRef): Receive = {
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
