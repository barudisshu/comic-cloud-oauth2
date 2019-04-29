package io.comiccloud.service.clients

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.clients._
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

object ClientAssociate {
  val Name = "client-associate"
  def props(clientsRepo: ClientsRepository, accountsRepo: AccountsRepository): Props =
    Props(new ClientAssociate(clientsRepo, accountsRepo))
}

class ClientAssociate(clientsRepo: ClientsRepository,
                      accountsRepo: AccountsRepository) extends Aggregate[ClientState, ClientEntity] {
  override def entityProps: Props = ClientEntity.props(clientsRepo, accountsRepo)
  override def receive: Receive = {
    case command: CreateClientCommand =>
      forwardCommand(command)

    case command: FindClientByIdCommand =>
      forwardCommand(command)

    case command: FindClientByAccountIdCommand =>
      forwardCommand(command)
  }
}
