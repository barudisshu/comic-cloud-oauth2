package io.comiccloud.service.accounts.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.accounts.request.{FindAccountByIdReq, FindAccountByUsernameReq}

object AccountFinder {
  def props(accountRepo: AccountsRepository): Props = Props(new AccountFinder(accountRepo))
}

class AccountFinder(accountRepo: AccountsRepository) extends Actor with ActorLogging with CommonBehaviorResolver {
  import context.dispatcher
  import akka.pattern.pipe
  override def receive: Receive = {
    case FindAccountByIdReq(id) =>
      context become resolveFindingAccountById(sender)
      accountRepo.findByUid(id) pipeTo self

    case FindAccountByUsernameReq(username) =>
      context become resolveFindingAccountById(sender)
      accountRepo.findByUsername(username) pipeTo self
  }
}
