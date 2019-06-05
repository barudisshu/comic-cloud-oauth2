package io.comiccloud.event.clients.factor

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.event.clients.ClientEntity.CreateValidatedClient
import io.comiccloud.event.clients._
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest._

import scala.concurrent.duration._
import scala.language.postfixOps

private[clients] object ClientCreateValidator {
  def props(clientsRepo: ClientsRepository, accountsRepo: AccountsRepository): Props =
    Props(new ClientCreateValidator(clientsRepo, accountsRepo))

  sealed trait State
  case object WaitingForRequest extends State
  case object ClientHasRespondedAccount extends State
  case object PersistenceRecord extends State

  sealed trait Data {
    def inputs: Inputs
  }

  case object NoData extends Data {
    override def inputs = Inputs(ActorRef.noSender, null)
  }

  case class Inputs(originator: ActorRef, request: CreateClientCommand)

  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }

  case class UnresolvedDependencies(inputs: Inputs) extends InputsData
  case class ResolvedDependencies(inputs: Inputs) extends InputsData
  case class LookedUpData(inputs: Inputs, client: ClientFO) extends InputsData

  val InvalidClientIdError = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))
  val RejectedClientError  = ErrorMessage("client.invalid.rejected", Some("The user does not exists"))

}

private[clients] class ClientCreateValidator(
                                              val clientsRepo: ClientsRepository,
                                              val accountsRepo: AccountsRepository) extends FSM[ClientCreateValidator
.State, ClientCreateValidator.Data] with ClientFactory {

  import ClientCreateValidator._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateClientCommand, _) =>
      findingByAccountId ! FindClientByAccountIdCommand(request.vo.ownerId)
      goto(ClientHasRespondedAccount) using ResolvedDependencies(Inputs(sender(), request))
  }

  when(ClientHasRespondedAccount, 5 seconds) {
    case Event(FullResult(_), ResolvedDependencies(inputs)) =>
      creator ! inputs.request
      goto(PersistenceRecord) using LookedUpData(inputs, inputs.request.vo)
    case Event(failure: Failure, data: ResolvedDependencies) =>
      data.originator ! failure
      stop
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the account")
      data.originator ! Failure(FailureType.Validation, RejectedClientError)
      stop
  }

  when(PersistenceRecord, 10 seconds) {
    case Event(FullResult(txn: ClientFO), data: LookedUpData) =>
      context.parent.tell(CreateValidatedClient(data.inputs.request), data.inputs.originator)
      stop
    case Event(failure: Failure, data: LookedUpData) =>
      data.originator ! failure
      stop
    case Event(empty: Empty, data: LookedUpData) =>
      data.originator ! empty
      stop
  }

  whenUnhandled {
    case Event(StateTimeout, data) =>
      log.error("Received state timeout in process to validate an order create request")
      data.inputs.originator ! unexpectedFail
      stop
    case Event(other, data) =>
      log.error("Received unexpected message of {} in state {}", other, stateName)
      data.inputs.originator ! unexpectedFail
      stop
  }

  def unexpectedFail = Failure(FailureType.Service, ServiceResult.UnexpectedFailure)

}
