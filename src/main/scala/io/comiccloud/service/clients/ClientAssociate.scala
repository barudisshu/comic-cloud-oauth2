package io.comiccloud.service.clients

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate

object ClientAssociate {
  val Name = "client-associate"
  def props(): Props =
    Props(new ClientAssociate())
}

class ClientAssociate() extends Aggregate[ClientState, ClientEntity] {
  override def entityProps: Props = ClientEntity.props()
  override def receive: Receive = {
    case command: CreateClientCommand =>
      forwardCommand(command)

    case command: FindClientByIdCommand =>
      forwardCommand(command)

    case command: FindClientByAccountIdCommand =>
      forwardCommand(command)
  }
}
