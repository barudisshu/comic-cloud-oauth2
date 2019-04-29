package io.comiccloud.event.codes

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.event.codes.factor.{CodeCreateValidator, CodeCreator}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

trait CodeFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def clientsRepo: ClientsRepository
  def accountsRepo: AccountsRepository

  def creator: ActorRef = context.actorOf(CodeCreator.props(clientsRepo))
  def validator: ActorRef = context.actorOf(CodeCreateValidator.props(clientsRepo, accountsRepo))
  def findingByAccountId: ActorRef = context.actorOf(CodeFindingByAccountId.props(clientsRepo, accountsRepo))

}
