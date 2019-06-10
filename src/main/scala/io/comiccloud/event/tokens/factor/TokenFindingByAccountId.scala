package io.comiccloud.event.tokens.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.accounts.AccountInfo
import io.comiccloud.event.tokens.FindTokenRelateAccountIdCommand
import io.comiccloud.models.Account
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.rest.{EmptyResult, FullResult}

private[tokens] object TokenFindingByAccountId {
  def props(repo: AccountsRepository): Props = Props(new TokenFindingByAccountId(repo))
}

class TokenFindingByAccountId(repo: AccountsRepository) extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case FindTokenRelateAccountIdCommand(accountId) =>
      context become findByAccountUid(sender)
      repo.findByUid(accountId) pipeTo self
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
