package io.comiccloud.service.tokens.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.request.FindTokenRelateAccountIdReq

private[tokens] object TokenFindingByAccountId {
  def props(accountRepo: AccountsRepository): Props = Props(new TokenFindingByAccountId(accountRepo))
}

class TokenFindingByAccountId(accountRepo: AccountsRepository)
    extends Actor
    with ActorLogging
    with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindTokenRelateAccountIdReq(accountId) =>
      context become resolveFindingAccountById(sender)
      accountRepo.findByUid(accountId) pipeTo self
  }

}
