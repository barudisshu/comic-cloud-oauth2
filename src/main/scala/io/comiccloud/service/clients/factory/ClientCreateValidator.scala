package io.comiccloud.service.clients.factory

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest.{ErrorMessage, Failure, FailureType, FullResult, _}
import io.comiccloud.service.clients.ClientActor.CreateValidatedClient
import io.comiccloud.service.clients.ClientFactory
import io.comiccloud.service.clients.request.{CreateClientReq, FindClientByAccountIdReq}
import io.comiccloud.service.clients.response.ClientResp

import scala.concurrent.duration._
import scala.language.postfixOps

private[clients] object ClientCreateValidator {
  def props(clientRepo: ClientsRepository, accountRepo: AccountsRepository): Props =
    Props(new ClientCreateValidator(clientRepo, accountRepo))

  sealed trait State
  case object WaitingForRequest         extends State
  case object ClientHasRespondedAccount extends State
  case object PersistenceRecord         extends State

  sealed trait Data {
    def inputs: Inputs
  }

  case object NoData extends Data {
    override def inputs = Inputs(ActorRef.noSender, null)
  }

  case class Inputs(originator: ActorRef, request: CreateClientReq)

  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }

  case class UnresolvedDependencies(inputs: Inputs)           extends InputsData
  case class ResolvedDependencies(inputs: Inputs)             extends InputsData
  case class LookedUpData(inputs: Inputs, client: ClientResp) extends InputsData

  val InvalidClientGrantTypeError = ErrorMessage("client.invalid.grantType", Some("The grant type is not supply"))
  val InvalidClientIdError        = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))
  val RejectedClientError         = ErrorMessage("client.invalid.rejected", Some("The user does not exists"))

  import io.comiccloud._

  val grantTypes: Seq[String] = Seq(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, PASSWORD, IMPLICIT)
}

private[clients] class ClientCreateValidator(val clientRepo: ClientsRepository, val accountRepo: AccountsRepository)
    extends FSM[ClientCreateValidator.State, ClientCreateValidator.Data]
    with ClientFactory {

  import ClientCreateValidator._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(CreateClientReq(client: ClientResp), _) if !grantTypes.contains(client.grantType) =>
      sender() ! Failure(FailureType.Validation, InvalidClientGrantTypeError)
      stop
    case Event(request: CreateClientReq, _) =>
      finder ! FindClientByAccountIdReq(request.vo.ownerId)
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
    case Event(FullResult(txn: ClientResp), data: LookedUpData) =>
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
