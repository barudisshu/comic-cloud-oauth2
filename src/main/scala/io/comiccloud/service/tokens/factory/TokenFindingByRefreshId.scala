package io.comiccloud.service.tokens.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.TokenDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.FindTokenRelateRefreshCommand

object TokenFindingByRefreshId {
  def props(): Props = Props(new TokenFindingByRefreshId())
}
class TokenFindingByRefreshId() extends Actor with ActorLogging with CommonBehaviorResolver {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case FindTokenRelateRefreshCommand(clientFO, refreshToken) =>
      context become resolveFindingRefreshById(clientFO, sender)
      TokenDatabase.TokenByRefreshTokenModel.getByTokenId(refreshToken) pipeTo self

  }
}
