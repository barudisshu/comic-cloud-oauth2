package io.comiccloud.service.clients

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.comiccloud.event.clients._
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

object ClientAssociate {
  val Name = "client-associate"
  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new ClientAssociate(accountsRepo, clientsRepo))
}

class ClientAssociate(accountsRepo: AccountsRepository,
                      clientsRepo: ClientsRepository) extends Actor with ActorLogging {
  val actorRef: ActorRef = context.actorOf(ClientEntity.props(accountsRepo, clientsRepo))
  override def receive: Receive = {
    case command: CreateClientCommand =>
      actorRef.forward(command)

    case command: FindClientByIdCommand =>
      actorRef.forward(command)

    case command: FindClientByAccountIdCommand =>
      actorRef.forward(command)
  }
}
