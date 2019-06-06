package io.comiccloud.event.tokens.factor

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.event.tokens.factor.TokenClientCredentialCreateValidator.{NoData, WaitingForRequest}
import io.comiccloud.event.tokens.{CreateClientCredentialTokenCommand, TokenFO, TokenFactory}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest.{ErrorMessage, Failure, FailureType, ServiceResult}

object TokenClientCredentialCreateValidator {
  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new TokenClientCredentialCreateValidator(accountsRepo, clientsRepo))

  sealed trait State
  case object WaitingForRequest extends State
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

  val InvalidUserIdError = ErrorMessage("user.invalid.userId", Some("You have supplied an invalid user id"))
  val RejectedUserError  = ErrorMessage("user.invalid.rejected", Some("The user already exists"))

}

  private[tokens] class TokenClientCredentialCreateValidator(
                                                           val accountsRepo: AccountsRepository,
                                                           val clientsRepo: ClientsRepository) extends
  FSM[TokenClientCredentialCreateValidator.State, TokenClientCredentialCreateValidator.Data] with TokenFactory {

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateClientCredentialTokenCommand, _) =>
      findingByAccountId ! CreateClientCredentialTokenCommand(request.vo)
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



