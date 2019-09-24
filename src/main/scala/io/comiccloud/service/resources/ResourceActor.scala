package io.comiccloud.service.resources

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.resources.request.CredentialsDeliverReq
import io.comiccloud.service.resources.response.ResourceResp
import io.comiccloud.util.ReplicatedCache
import io.comiccloud.util.ReplicatedCache.{Cached, GetFromCache}

object ResourceActor {

  val Name = "resource"
  def props(): Props =
    Props(new ResourceActor())
}

class ResourceActor() extends Actor with ActorLogging {

  var replyTo: ActorRef = _

  val cache: ActorRef = context.actorOf(ReplicatedCache.props)

  override def receive: Receive = {
    case CredentialsDeliverReq(vo) =>
      replyTo = sender
      cache.tell(GetFromCache(s"@${vo.token}"), self)
    case Cached(_, Some(v)) =>
      ResourceResp.parall(v) match {
        case Some(value) =>
          replyTo ! FullResult(value)
        case None =>
          replyTo ! EmptyResult
      }

    case Cached(_, None) =>
      replyTo ! EmptyResult

    case _ =>
      replyTo ! EmptyResult

  }
}
