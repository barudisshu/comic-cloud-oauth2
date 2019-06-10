package io.comiccloud.event.clients

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest.FullResult

object ClientEntity {
  val Name = "client"

  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new ClientEntity(accountsRepo, clientsRepo))

  case class CreateClient(ccc: CreateClientCommand)
  case class CreateValidatedClient(ccv: CreateClientCommand)
}

class ClientEntity(val accountsRepo: AccountsRepository,
                   val clientsRepo: ClientsRepository) extends Actor with ActorLogging with ClientFactory {

  import ClientEntity._

  override def receive: Receive = {
    case o: CreateClientCommand =>
      validator.forward(o)

    case CreateValidatedClient(cmd) =>
      sender() ! FullResult(cmd.vo)

    case cmd: FindClientByAccountIdCommand =>
      findingByAccountId.forward(cmd)

    case CreateClient(cmd) =>
      creator.forward(cmd)
  }
}
