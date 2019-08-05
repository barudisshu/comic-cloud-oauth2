package io.comiccloud.service.clients

import akka.actor.Props
import io.comiccloud.entity.PersistentEntity

object ClientEntity {
  val Name = "client"

  def props(): Props =
    Props(new ClientEntity())

  case class CreateClient(ccc: CreateClientCommand)
  case class CreateValidatedClient(ccv: CreateClientCommand)
}

class ClientEntity() extends PersistentEntity[ClientState] with ClientFactory {

  import ClientEntity._

  override def initialState: ClientState = ClientInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CreateClientCommand =>
      validator.forward(o)
      state = ValidationFO.validation

    case CreateValidatedClient(cmd) =>
      state = cmd.vo
      handleResponse()

    case cmd: FindClientByAccountIdCommand =>
      findingByAccountId.forward(cmd)

    case CreateClient(cmd) =>
      creator.forward(cmd)

  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case ccc: CreateClientCommand   => true
    case cvc: CreateValidatedClient => true
    case _                          => false
  }
}
