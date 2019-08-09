package io.comiccloud.service.tokens.factory.authorization_code

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.rest.{ErrorMessage, Failure, FailureType, ServiceResult}
import io.comiccloud.service.tokens._

object TokenAuthorizationCodeCreateValidator {
  def props(): Props = Props(new TokenAuthorizationCodeCreateValidator())

  sealed trait State
  case object WaitingForRequest        extends State
  case object TokenHasRespondedAccount extends State
  case object LookingUpEntities        extends State
  case object InsertDb                 extends State

  sealed trait Data {
    def inputs: Inputs
  }
  case object NoData extends Data {
    def inputs = Inputs(ActorRef.noSender, null)
  }
  case class Inputs(originator: ActorRef, request: CreateAuthorizationCodeTokenCommand)
  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }
  case class UnresolvedDependencies(inputs: Inputs)      extends InputsData
  case class ResolvedDependencies(inputs: Inputs)        extends InputsData
  case class LookedUpData(inputs: Inputs, user: TokenFO) extends InputsData

  object ResolutionIdent extends Enumeration {
    val Token: Value = Value
  }

  val InvalidClientIdError = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))
  val InvalidCodeIdError   = ErrorMessage("code.invalid.codeId", Some("You have supplied an invalid code"))

}

private[tokens] class TokenAuthorizationCodeCreateValidator()
    extends FSM[TokenAuthorizationCodeCreateValidator.State, TokenAuthorizationCodeCreateValidator.Data]
    with TokenFactory {

  import TokenAuthorizationCodeCreateValidator._

  startWith(WaitingForRequest, NoData)

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
