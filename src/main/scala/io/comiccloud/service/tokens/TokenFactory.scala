package io.comiccloud.service.tokens

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.service.tokens.factory._
import io.comiccloud.service.tokens.factory.credentials._
import io.comiccloud.service.tokens.factory.authorization_code._
import io.comiccloud.service.tokens.factory.password._
import io.comiccloud.service.tokens.factory.refresh_token._

trait TokenFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def clientCredentials: ActorRef = context.actorOf(TokenClientCredentialsCreateValidator.props())
  def authorizationCode: ActorRef= context.actorOf(TokenAuthorizationCodeCreateValidator.props())
  def password: ActorRef = context.actorOf(TokenPasswordCreateValidator.props())
  def refreshToken: ActorRef = context.actorOf(TokenRefreshTokenCreateValidator.props())

  def tokenCreator: ActorRef = context.actorOf(TokenValidatedCreator.props())
  def findingByAccountId: ActorRef = context.actorOf(TokenFindingByAccountId.props())
  def findingByClientId: ActorRef = context.actorOf(TokenFindingByClientIdAndKey.props())
  def findingByCodeId: ActorRef = context.actorOf(TokenFindingByCodeId.props())
  def deletingByCodeId: ActorRef = context.actorOf(TokenDeletingCodeId.props())
  def findingByRefreshToken: ActorRef = context.actorOf(TokenFindingByRefreshId.props())
  def deleteAccessTokenId: ActorRef = context.actorOf(TokenDeletingAccessTokenId.props())

}