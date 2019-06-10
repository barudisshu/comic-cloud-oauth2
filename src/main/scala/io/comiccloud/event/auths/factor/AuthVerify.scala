package io.comiccloud.event.auths.factor

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.event.auths._
import io.comiccloud.event.auths.factor.AuthVerify._
import io.comiccloud.rest._

import scala.language.postfixOps

object AuthVerify {
  def props(): Props = Props(new AuthVerify())

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
  case class Inputs(originator: ActorRef, request: VerificationAuthCommand)
  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }
  case class UnresolvedDependencies(inputs: Inputs) extends InputsData
  case class ResolvedDependencies(inputs: Inputs) extends InputsData
  case class LookedUpData(inputs: Inputs, user: AuthFO) extends InputsData

  object ResolutionIdent extends Enumeration {
    val Token: Value = Value
  }

  val InvalidClientIdError = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))

}

private[auths] class AuthVerify() extends FSM[AuthVerify.State, AuthVerify.Data] with AuthFactory {
  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: VerificationAuthCommand, _) =>
      context.parent.tell(VerifiedAuthCommand(request.vo), sender)
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
