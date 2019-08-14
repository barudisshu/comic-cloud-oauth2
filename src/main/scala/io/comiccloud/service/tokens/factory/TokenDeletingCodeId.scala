package io.comiccloud.service.tokens.factory

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.CodeDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.DeleteTokenRelateCodeCommand

object TokenDeletingCodeId {
  def props(): Props = Props(new TokenDeletingCodeId())
}

class TokenDeletingCodeId() extends Actor with ActorLogging with CommonBehaviorResolver {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case DeleteTokenRelateCodeCommand(code) =>
    context become resolveDeletingCodeById(code, sender)
      CodeDatabase.CodeModel.deleteById(code) pipeTo self
  }
}
