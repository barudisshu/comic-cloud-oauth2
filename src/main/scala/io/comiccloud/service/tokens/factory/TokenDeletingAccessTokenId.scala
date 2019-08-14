package io.comiccloud.service.tokens.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.TokenDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.DeleteTokenRelateAccessCommand

object TokenDeletingAccessTokenId {
  def props(): Props = Props(new TokenDeletingAccessTokenId())
}
class TokenDeletingAccessTokenId() extends Actor with ActorLogging with CommonBehaviorResolver{

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case DeleteTokenRelateAccessCommand(accessToken) =>
    context become resolveDeletingAccessToken(accessToken, sender)
      TokenDatabase.deleteByAccessToken(accessToken) pipeTo self
  }
}
