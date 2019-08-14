package io.comiccloud.service.accounts

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate

object AccountAssociate {
  val Name           = "account-associate"
  def props(): Props = Props(new AccountAssociate())
}

class AccountAssociate() extends Aggregate[AccountState, AccountEntity] {
  override def entityProps: Props = AccountEntity.props()

  override def receive: Receive = {
    case command: CreateAccountCommand =>
      forwardCommandWithoutSharding(command)

    case command: FindAccountByIdCommand =>
      forwardCommandWithoutSharding(command)
  }
}
