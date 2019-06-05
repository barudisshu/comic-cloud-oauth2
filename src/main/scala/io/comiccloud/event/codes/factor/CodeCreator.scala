package io.comiccloud.event.codes.factor

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import io.comiccloud.event.codes._
import io.comiccloud.rest.{EmptyResult, FullResult}

/**
  * the code will be persist in redis, for a while ...
  */
private[codes] object CodeCreator {
  def props(): Props = Props(new CodeCreator())
}

class CodeCreator() extends Actor with ActorLogging {

  override def receive: Receive = {
    case CreateCodeCommand(vo) =>
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
