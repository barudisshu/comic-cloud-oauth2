package io.comiccloud.service.codes.factor

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.codes.request.CreateCodeReq

private[codes] object CodeCreator {
  def props(): Props = Props(new CodeCreator())
}

class CodeCreator() extends Actor with ActorLogging {

  override def receive: Receive = {
    case CreateCodeReq(vo) =>
      sender() ! FullResult(vo)
      self ! PoisonPill

    case f: akka.actor.Status.Failure =>
      sender ! EmptyResult
      context stop self
    case None =>
      sender ! EmptyResult
      context stop self

  }

}
