package io.comiccloud.event.codes.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.codes._
import io.comiccloud.models.Client
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.rest.{EmptyResult, ErrorMessage, Failure, FailureType, FullResult}

/**
  * the code will be persist in redis, for a while ...
  */
private[codes] object CodeCreator {
  def props: Props = Props(new CodeCreator())
}

class CodeCreator() extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = ???
}
