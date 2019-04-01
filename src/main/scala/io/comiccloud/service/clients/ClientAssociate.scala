package io.comiccloud.service.clients

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.clients._
import io.comiccloud.repository.ClientsRepository

object ClientAssociate {
  val Name = "client-associate"
  def props(repository: ClientsRepository): Props = Props(new ClientAssociate(repository))
}

class ClientAssociate(repository: ClientsRepository) extends Aggregate[ClientState, ClientEntity] {
  override def entityProps: Props = ClientEntity.props(repository)
  override def receive: Receive = {
    case command: CreateClientCommand =>
      forwardCommand(command)

    case command: FindClientByIdCommand =>
      forwardCommand(command)

    case command: FindClientByAccountIdCommand =>
      forwardCommand(command)
  }
}
