package io.comiccloud.service.tokens

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.service.tokens.factory._
import io.comiccloud.service.tokens.factory.credentials._
import io.comiccloud.service.tokens.factory.authorization_code._

trait TokenFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def clientCredential: ActorRef = context.actorOf(TokenClientCredentialsCreateValidator.props())
  def authorizationCode: ActorRef= context.actorOf(TokenAuthorizationCodeCreateValidator.props())

  def tokenCreator: ActorRef = context.actorOf(TokenValidatedCreator.props())
  def findingByAccountId: ActorRef = context.actorOf(TokenFindingByAccountId.props())
  def findingByClientId: ActorRef = context.actorOf(TokenFindingByClientIdAndKey.props())
  def findingByCodeId: ActorRef = context.actorOf(TokenFindingByCodeId.props())

}