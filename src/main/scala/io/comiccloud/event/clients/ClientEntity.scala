package io.comiccloud.event.clients

import akka.actor.Props
import io.comiccloud.entity.{EntityEvent, PersistentEntity}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

object ClientEntity {
  val Name = "client"

  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new ClientEntity(accountsRepo, clientsRepo))

  case class CreateClient(ccc: CreateClientCommand)
  case class CreateValidatedClient(ccv: CreateClientCommand)
}

class ClientEntity(val accountsRepo: AccountsRepository,
                   val clientsRepo: ClientsRepository) extends PersistentEntity[ClientState] with ClientFactory {

  import ClientEntity._

  override def initialState: ClientState = ClientInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CreateClientCommand =>
      validator.forward(o)
      state = ValidationFO.validation

    case CreateValidatedClient(cmd) =>
      val state = cmd.vo
      persistAsync(ClientCreatedEvent(state))(handleEventAndRespond())

    case cmd: FindClientByAccountIdCommand =>
      findingByAccountId.forward(cmd)

    case CreateClient(cmd) =>
      creator.forward(cmd)

  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case ccc: CreateClientCommand => true
    case cvc: CreateValidatedClient => true
    case _ => false
  }

  override def handleEvent(event: EntityEvent): Unit = event match {
    case ClientCreatedEvent(clientFO) =>
      state = clientFO
  }
}
