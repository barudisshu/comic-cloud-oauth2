package io.comiccloud.event.clients

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.event.clients.factor._
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

trait ClientFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def clientsRepo: ClientsRepository
  def accountsRepo: AccountsRepository

  def creator: ActorRef = context.actorOf(ClientCreator.props(clientsRepo))
  def validator: ActorRef = context.actorOf(ClientCreateValidator.props(clientsRepo, accountsRepo))
  def findingByAccountId: ActorRef = context.actorOf(ClientFindingByAccountId.props(clientsRepo, accountsRepo))
}
