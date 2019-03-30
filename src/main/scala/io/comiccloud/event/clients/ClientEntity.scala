package io.comiccloud.event.clients

import akka.actor.Props
import io.comiccloud.entity.{EntityEvent, PersistentEntity}
import io.comiccloud.repository.ClientsRepository

object ClientEntity {
  val Name = "client"

  def props(repo: ClientsRepository): Props = Props(new ClientEntity(repo))

  case class CreateClient(ccc: CreateClientCommand)
  case class CreateClientValidation(ccv: CreateClientCommand)
}

class ClientEntity(val repo: ClientsRepository) extends PersistentEntity[ClientState] with ClientFactory {
  import ClientEntity._

  override def initialState: ClientState = ClientInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CreateClientCommand =>
      //
  }

  override def isCreateMessage(cmd: Any): Boolean = ???
  override def handleEvent(event: EntityEvent): Unit = ???
}
