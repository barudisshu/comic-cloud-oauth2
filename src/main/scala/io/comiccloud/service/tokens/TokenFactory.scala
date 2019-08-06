package io.comiccloud.service.tokens

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.service.tokens.factory._

trait TokenFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def clientCredential: ActorRef = context.actorOf(TokenClientCredentialCreateValidator.props())
  def clientCreator: ActorRef = context.actorOf(TokenClientCredentialCreator.props())

  def findingByAccountId: ActorRef = context.actorOf(TokenFindingByAccountId.props())
  def findingByClientId: ActorRef = context.actorOf(TokenFindingByClient.props())

}