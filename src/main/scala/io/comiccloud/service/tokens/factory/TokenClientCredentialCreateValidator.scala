package io.comiccloud.service.tokens.factory

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.rest._
import io.comiccloud.service.clients.ClientFO
import io.comiccloud.service.tokens._
import io.comiccloud.service.tokens.factory.TokenClientCredentialCreateValidator._

import scala.concurrent.duration._
import scala.language.postfixOps

object TokenClientCredentialCreateValidator {
  def props(): Props =    Props(new TokenClientCredentialCreateValidator())

  sealed trait State
  case object WaitingForRequest extends State
  case object TokenHasRespondedAccount extends State
  case object LookingUpEntities extends State
  case object InsertDb extends State

  sealed trait Data {
    def inputs: Inputs
  }
  case object NoData extends Data {
    def inputs = Inputs(ActorRef.noSender, null)
  }
  case class Inputs(originator: ActorRef, request: CreateClientCredentialTokenCommand)
  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }
  case class UnresolvedDependencies(inputs: Inputs) extends InputsData
  case class ResolvedDependencies(inputs: Inputs) extends InputsData
  case class LookedUpData(inputs: Inputs, user: TokenFO) extends InputsData

  object ResolutionIdent extends Enumeration {
    val Token: Value = Value
  }

  val InvalidClientIdError = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))

}

private[tokens] class TokenClientCredentialCreateValidator() extends
  FSM[TokenClientCredentialCreateValidator.State, TokenClientCredentialCreateValidator.Data] with TokenFactory {

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateClientCredentialTokenCommand, _) =>
      findingByClientId ! FindTokenRelateClientCommand(request.vo.appid, request.vo.appkey)
      goto(TokenHasRespondedAccount) using ResolvedDependencies(Inputs(sender, request))
  }

  when(TokenHasRespondedAccount, 5 seconds) {
    case Event(FullResult(clientFO: ClientFO), data@ResolvedDependencies(inputs)) =>
      context.parent.tell(CreateValidatedClientCredentialTokenCommand(inputs.request.vo.copy(accountId = clientFO.ownerId)), data.inputs.originator)
      stop
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the client")
      data.originator ! Failure(FailureType.Validation, InvalidClientIdError)
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