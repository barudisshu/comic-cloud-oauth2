package io.comiccloud.service.tokens.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.CodeDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.FindTokenRelateCodeCommand

private[tokens] object TokenFindingByCodeId {
  def props(): Props = Props(new TokenFindingByCodeId())
}

class TokenFindingByCodeId() extends Actor with ActorLogging with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case FindTokenRelateCodeCommand(codeId) =>
      context become resolveFindingCodeById(sender)
      CodeDatabase.CodeModel.getById(codeId) pipeTo self
  }
}
