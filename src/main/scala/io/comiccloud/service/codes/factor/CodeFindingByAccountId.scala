package io.comiccloud.service.codes.factor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.AccountDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.codes.FindCodeRelateAccountIdCommand

object CodeFindingByAccountId {
  def props(): Props = Props(new CodeFindingByAccountId())
}

class CodeFindingByAccountId() extends Actor with ActorLogging with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindCodeRelateAccountIdCommand(accountId) =>
      context become resolveFindingAccountById(sender)
      AccountDatabase.AccountModel.getByAccountId(UUID.fromString(accountId)) pipeTo self
  }
}
