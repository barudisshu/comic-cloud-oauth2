package io.comiccloud.service.accounts

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.service.accounts.factory.{AccountCreateValidator, AccountCreator, AccountFindingById}

trait AccountFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def validator: ActorRef = context.actorOf(AccountCreateValidator.props())
  def creator: ActorRef = context.actorOf(AccountCreator.props())
  def findingById: ActorRef = context.actorOf(AccountFindingById.props())
}
