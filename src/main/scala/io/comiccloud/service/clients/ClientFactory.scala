package io.comiccloud.service.clients

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.service.clients.factory._

trait ClientFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def creator: ActorRef = context.actorOf(ClientCreator.props())
  def validator: ActorRef = context.actorOf(ClientCreateValidator.props())
  def findingByAccountId: ActorRef = context.actorOf(ClientFinder.props())
}