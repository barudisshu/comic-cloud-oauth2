package io.comiccloud.service.tokens.factory

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, ReceiveTimeout}
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.tokens.request.CreateTokenReq
import io.comiccloud.util.ReplicatedCache
import io.comiccloud.util.ReplicatedCache.PutInCache

private[tokens] object TokenCreator {
  def props(): Props = Props(new TokenCreator())
}

class TokenCreator() extends Actor with ActorLogging with CommonBehaviorResolver {
  val cache: ActorRef = context.actorOf(ReplicatedCache.props)

  override def receive: Receive = {
    case CreateTokenReq(vo) =>
      cache ! PutInCache(s"@${vo.accessToken}", vo)
      cache ! PutInCache(s"#${vo.refreshToken}", vo)
      sender ! FullResult(vo)
      self ! PoisonPill
    case ReceiveTimeout =>
      sender ! EmptyResult
      self ! PoisonPill
  }
}
