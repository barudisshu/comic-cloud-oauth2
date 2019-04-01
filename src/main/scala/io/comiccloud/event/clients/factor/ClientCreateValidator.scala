package io.comiccloud.event.clients.factor

import akka.actor.{FSM, Props}
import io.comiccloud.event.clients.ClientFactory
import io.comiccloud.repository.ClientsRepository
import io.comiccloud.rest.{Failure, FailureType, ServiceResult}

private[clients] object ClientCreateValidator {
  def props(repo: ClientsRepository): Props = Props(new ClientCreateValidator(repo))

  sealed trait State

  sealed trait Data
}

private[client] class ClientCreateValidator(val repo: ClientsRepository) extends FSM[ClientCreateValidator.State, ClientCreateValidator.Data] with ClientFactory {

  import ClientCreateValidator._

  // todo:

  def unexpectedFail = Failure(FailureType.Service, ServiceResult.UnexpectedFailure)

}
