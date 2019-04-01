package io.comiccloud.event.clients

import akka.actor.Props
import io.comiccloud.entity.{EntityEvent, PersistentEntity}
import io.comiccloud.repository.ClientsRepository

object ClientEntity {
  val Name = "client"

  def props(repo: ClientsRepository): Props = Props(new ClientEntity(repo))

  case class CreateClient(ccc: CreateClientCommand)
  case class CreateValidatedClient(ccv: CreateClientCommand)
}

class ClientEntity(val repo: ClientsRepository) extends PersistentEntity[ClientState] with ClientFactory {
  import ClientEntity._

  override def initialState: ClientState = ClientInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CreateClientCommand =>
      // before create, valid the accountId exists
      validator.forward(o)
      state = ValidationFO.validation

    case CreateValidatedClient(cmd) =>
      val state = cmd.vo
      persistAsync(ClientCreatedEvent(state))(handleEventAndRespond())

    // ========================================================================
    // atomicity operator show as below
    // ========================================================================

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
