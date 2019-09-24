package io.comiccloud.service.codes.factor

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.codes.request.FindCodeRelateAccountIdReq

object CodeFindingByAccountId {
  def props(accountRepo: AccountsRepository): Props = Props(new CodeFindingByAccountId(accountRepo))
}

class CodeFindingByAccountId(accountRepo: AccountsRepository)
    extends Actor
    with ActorLogging
    with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case FindCodeRelateAccountIdReq(accountId) =>
      context become resolveFindingAccountById(sender)
      accountRepo.findByUid(accountId) pipeTo self
  }
}
