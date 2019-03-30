package io.comiccloud.event.clients

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.repository.ClientsRepository

trait ClientFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def repo: ClientsRepository

  def creation: ActorRef = context.actorOf(ClientCreation.props(repo))
  def validation: ActorRef = context.actorOf(ClientValidation.props(repo))
}
