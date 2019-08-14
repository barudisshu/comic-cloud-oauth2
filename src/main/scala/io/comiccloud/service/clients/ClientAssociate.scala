package io.comiccloud.service.clients

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate

object ClientAssociate {
  val Name           = "client-associate"
  def props(): Props = Props(new ClientAssociate())
}

class ClientAssociate() extends Aggregate[ClientState, ClientEntity] {
  override def entityProps: Props = ClientEntity.props()
  override def receive: Receive = {
    case command: CreateClientCommand =>
      forwardCommandWithoutSharding(command)

    case command: FindClientByIdCommand =>
      forwardCommandWithoutSharding(command)

    case command: FindClientByAccountIdCommand =>
      forwardCommandWithoutSharding(command)
  }
}
