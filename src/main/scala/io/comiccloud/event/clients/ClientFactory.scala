package io.comiccloud.event.clients

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.repository.ClientsRepository

trait ClientFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def repo: ClientsRepository

  def creator: ActorRef = context.actorOf(ClientCreator.props(repo))
  def validator: ActorRef = context.actorOf(ClientCreateValidator.props(repo))
  def findingByAccountId: ActorRef = context.actorOf(ClientFindingByAccountId.props(repo))
}
