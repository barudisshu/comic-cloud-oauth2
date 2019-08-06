package io.comiccloud.service.accounts.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.AccountDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.accounts.{FindAccountByIdCommand, FindAccountByUsernameCommand}

object AccountFinder {
  def props(): Props = Props(new AccountFinder())
}

class AccountFinder() extends Actor with ActorLogging with CommonBehaviorResolver {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindAccountByIdCommand(id) =>
      context become resolveFindingAccountById(sender)
      AccountDatabase.AccountModel.getByAccountId(UUID.fromString(id)) pipeTo self

    case FindAccountByUsernameCommand(_, username) =>
      context become resolveFindingAccountById(sender)
      AccountDatabase.AccountModel.getByAccountUsername(username).map(_.headOption) pipeTo self
  }
}
