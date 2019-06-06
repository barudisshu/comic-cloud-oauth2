package io.comiccloud.event.tokens

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.event.tokens.factor._
import io.comiccloud.repository._

trait TokenFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def accountsRepo: AccountsRepository
  def clientsRepo: ClientsRepository

  def clientCredential: ActorRef = context.actorOf(TokenClientCredentialCreateValidator.props(accountsRepo, clientsRepo))
  def clientCreator: ActorRef = context.actorOf(TokenClientCredentialCreator.props())

  def findingByAccountId: ActorRef = context.actorOf(TokenFindingByAccountId.props(accountsRepo))
  def findingByClientId: ActorRef = context.actorOf(TokenFindingByClientId.props(clientsRepo))

}
