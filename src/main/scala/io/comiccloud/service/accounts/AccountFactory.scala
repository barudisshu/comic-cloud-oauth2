package io.comiccloud.service.accounts

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.service.accounts.factory._

trait AccountFactory {
  this: Actor with ActorLogging =>

  val accountRepo: AccountsRepository

  def validator: ActorRef = context.actorOf(AccountCreateValidator.props(accountRepo))
  def creator: ActorRef = context.actorOf(AccountCreator.props(accountRepo))
  def findingById: ActorRef = context.actorOf(AccountFinder.props(accountRepo))

}
