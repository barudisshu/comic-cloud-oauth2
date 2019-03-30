package io.comiccloud.service.accounts

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.accounts._
import io.comiccloud.repository.AccountsRepository

object AccountAssociate {
  val Name = "account-associate"
  def props(repository: AccountsRepository): Props = Props(new AccountAssociate(repository))
}

class AccountAssociate(repository: AccountsRepository) extends Aggregate[AccountState, AccountEntity] {
  override def entityProps: Props = AccountEntity.props(repository)

  override def receive: Receive = {
    case command: CreateAccountCommand =>
      forwardCommand(command)

    case command: FindAccountByIdCommand =>
      forwardCommand(command)
  }
}
