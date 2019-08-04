package io.comiccloud.event.resources

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.event.resources.factor.ResourceCredentialHandler

trait ResourceFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def resourceCredential: ActorRef = context.actorOf(ResourceCredentialHandler.props())
}