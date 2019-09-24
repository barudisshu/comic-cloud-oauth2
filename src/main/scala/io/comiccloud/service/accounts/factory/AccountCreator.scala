package io.comiccloud.service.accounts.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.models.Account
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.accounts.request.CreateAccountReq

object AccountCreator {
  def props(accountRepo: AccountsRepository): Props = Props(new AccountCreator(accountRepo))
}

class AccountCreator(accountRepo: AccountsRepository) extends Actor with ActorLogging with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case CreateAccountReq(vo) =>
      val account = Account(
        uid = vo.id,
        username = vo.username,
        password = vo.password,
        salt = vo.salt,
        email = vo.email,
        phone = vo.phone,
        createdAt = vo.createdAt
      )
      context become feedback[Account](vo, sender)
      accountRepo.insert(account) pipeTo self
  }
}
