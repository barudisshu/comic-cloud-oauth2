package io.comiccloud.event.accounts

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.repository.AccountsRepository

/**
  * these is a atomicity actor, just work for one thing
  */
trait AccountFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def repo: AccountsRepository

  def validator: ActorRef = context.actorOf(AccountCreateValidator.props(repo))
  def creation: ActorRef = context.actorOf(AccountCreation.props(repo))
  def finding: ActorRef = context.actorOf(AccountFindingById.props(repo))
}
