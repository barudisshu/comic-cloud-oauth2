package io.comiccloud.service.tokens.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.AccountDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.FindTokenRelateAccountIdCommand

private[tokens] object TokenFindingByAccountId {
  def props(): Props = Props(new TokenFindingByAccountId())
}

class TokenFindingByAccountId() extends Actor with ActorLogging with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case FindTokenRelateAccountIdCommand(accountId) =>
      context become resolveFindingAccountById(sender)
      AccountDatabase.AccountModel.getByAccountId(UUID.fromString(accountId)) pipeTo self
  }
}
