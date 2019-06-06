package io.comiccloud.service.clients

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.clients._
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

object ClientAssociate {
  val Name = "client-associate"
  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new ClientAssociate(accountsRepo, clientsRepo))
}

class ClientAssociate(accountsRepo: AccountsRepository,
                      clientsRepo: ClientsRepository) extends Aggregate[ClientState, ClientEntity] {
  override def entityProps: Props = ClientEntity.props(accountsRepo, clientsRepo)
  override def receive: Receive = {
    case command: CreateClientCommand =>
      forwardCommand(command)

    case command: FindClientByIdCommand =>
      forwardCommand(command)

    case command: FindClientByAccountIdCommand =>
      forwardCommand(command)
  }
}
