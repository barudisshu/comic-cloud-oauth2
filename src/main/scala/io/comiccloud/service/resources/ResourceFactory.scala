package io.comiccloud.service.resources

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.service.resources.factory._

trait ResourceFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def resourceCredential: ActorRef = context.actorOf(ResourceCredentialHandler.props())
  def findingByTokenId: ActorRef = context.actorOf(ResourceFindingByToken.props())
  def findingByAccountId: ActorRef = context.actorOf(ResourceFindingByAccountId.props())
  def findingByClientId: ActorRef = context.actorOf(ResourceFindingByClientId.props())
  def resourceComposer: ActorRef = context.actorOf(ResourceComposer.props())
}