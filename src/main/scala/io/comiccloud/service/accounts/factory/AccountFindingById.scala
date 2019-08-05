package io.comiccloud.service.accounts.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.modeling.database.AccountDatabase
import io.comiccloud.modeling.entity.Account
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.accounts.{AccountFO, FindAccountByIdCommand, FindAccountByUsernameCommand}

object AccountFindingById {
  def props(): Props = Props(new AccountFindingById())
}

class AccountFindingById() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindAccountByIdCommand(id) =>
      context become fetchRecord(sender)
      AccountDatabase.AccountModel.getByAccountId(UUID.fromString(id)) pipeTo self

    case FindAccountByUsernameCommand(_, username) =>
    context become fetchRecord(sender)
    AccountDatabase.AccountModel.getByAccountUsername(username) pipeTo self
  }

  def fetchRecord(replyTo: ActorRef): Receive = {
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
