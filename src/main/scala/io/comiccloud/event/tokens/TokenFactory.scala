package io.comiccloud.event.tokens

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.event.tokens.factor.{TokenClientCredentialCreateValidator, TokenFindingByAccountId}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

trait TokenFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def accountsRepo: AccountsRepository
  def clientsRepo: ClientsRepository

  def clientCredential: ActorRef = context.actorOf(TokenClientCredentialCreateValidator.props(accountsRepo, clientsRepo))
  def findingByAccountId: ActorRef = context.actorOf(TokenFindingByAccountId.props(accountsRepo))

}
