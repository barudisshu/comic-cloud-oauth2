package io.comiccloud.event.clients.factor

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.event.clients.CreateClientCommand
import io.comiccloud.repository.ClientsRepository

private[clients] object ClientCreator {
  def props(repo: ClientsRepository): Props = Props(new ClientCreator(repo))
}

class ClientCreator(repo: ClientsRepository) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case CreateClientCommand(vo) =>
  }
}
