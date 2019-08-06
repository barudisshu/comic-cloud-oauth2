package io.comiccloud.service.resources

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.service.resources.factory.ResourceCredentialHandler

trait ResourceFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def resourceCredential: ActorRef = context.actorOf(ResourceCredentialHandler.props())
}