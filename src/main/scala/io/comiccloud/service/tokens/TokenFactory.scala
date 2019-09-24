package io.comiccloud.service.tokens

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.service.tokens.factory.authorization_code.TokenAuthorizationCodeCreateValidator
import io.comiccloud.service.tokens.factory.credentials.TokenClientCredentialsCreateValidator
import io.comiccloud.service.tokens.factory.password.TokenPasswordCreateValidator
import io.comiccloud.service.tokens.factory.refresh_token.TokenRefreshTokenCreateValidator
import io.comiccloud.service.tokens.factory.{TokenCreator, TokenFindingByAccountId, TokenFindingByClientIdAndKey, TokenFindingByCodeId}

trait TokenFactory {
  this: Actor with ActorLogging =>

  val accountRepo: AccountsRepository
  val clientRepo : ClientsRepository
  val codeRef   : ActorRef

  def creator: ActorRef = context.actorOf(TokenCreator.props())
  def clientCredentials: ActorRef = context.actorOf(TokenClientCredentialsCreateValidator.props(accountRepo, clientRepo, codeRef))
  def authorizationCode: ActorRef = context.actorOf(TokenAuthorizationCodeCreateValidator.props(accountRepo, clientRepo, codeRef))
  def password: ActorRef = context.actorOf(TokenPasswordCreateValidator.props(accountRepo, clientRepo, codeRef))
  def refreshToken: ActorRef = context.actorOf(TokenRefreshTokenCreateValidator.props(accountRepo, clientRepo, codeRef))

  def findingByAccountId: ActorRef = context.actorOf(TokenFindingByAccountId.props(accountRepo))
  def findingByClientId: ActorRef = context.actorOf(TokenFindingByClientIdAndKey.props(clientRepo))
  def findingByCodeId: ActorRef = context.actorOf(TokenFindingByCodeId.props(codeRef))
}
