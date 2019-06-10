package io.comiccloud.service.accounts

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.comiccloud.event.accounts._
import io.comiccloud.repository.AccountsRepository

object AccountAssociate {
  val Name = "account-associate"
  def props(repository: AccountsRepository): Props = Props(new AccountAssociate(repository))
}

class AccountAssociate(repository: AccountsRepository) extends Actor with ActorLogging {
  val actorRef: ActorRef = context.actorOf(AccountEntity.props(repository))
  override def receive: Receive = {
    case command: CreateAccountCommand =>
      actorRef.forward(command)
    case command: FindAccountByIdCommand =>
      actorRef.forward(command)
  }
}