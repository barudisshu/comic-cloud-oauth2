package io.comiccloud.event.codes.factor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.util.ByteString
import io.comiccloud.event.codes.FindCodeByClientIdCommand
import io.comiccloud.rest.{EmptyResult, FullResult}
import org.apache.commons.lang3.SerializationUtils

private[codes] object CodeConsumer {
  def props(): Props = Props(new CodeConsumer())
}

class CodeConsumer() extends Actor with ActorLogging {
  override def receive: Receive = {
    case FindCodeByClientIdCommand(clientId) =>
  }
}
