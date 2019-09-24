package io.comiccloud.service.tokens.factory

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.codes.CodeActor.StopEntity
import io.comiccloud.service.codes.request.FindCodeByIdReq
import io.comiccloud.service.tokens.request.FindTokenRelateCodeReq

private[tokens] object TokenFindingByCodeId {
  def props(codeRef: ActorRef): Props = Props(new TokenFindingByCodeId(codeRef))
}

class TokenFindingByCodeId(codeRef: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case FindTokenRelateCodeReq(codeId) =>
      context become destroy(sender)
      codeRef.tell(FindCodeByIdReq(codeId), self)
  }

  // after fetching the code, the sharding will be destroyed
  private def destroy(replyTo: ActorRef): Receive = {
    case f@FullResult(_) =>
      sender ! StopEntity
      replyTo ! f
    case e@EmptyResult =>
      sender ! StopEntity
    replyTo ! e
  }
}
