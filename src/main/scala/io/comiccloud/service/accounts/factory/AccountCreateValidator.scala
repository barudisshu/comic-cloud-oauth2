package io.comiccloud.service.accounts.factory

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.rest._
import io.comiccloud.service.accounts.AccountEntity.CreateValidatedAccount
import io.comiccloud.service.accounts.{AccountFO, AccountFactory, CreateAccountCommand, FindAccountByIdCommand, FindAccountByUsernameCommand}

import scala.concurrent.duration._
import scala.language.postfixOps

private[accounts] object AccountCreateValidator {
  def props() = Props(new AccountCreateValidator())

  sealed trait State
  case object WaitingForRequest extends State
  case object LookingUpEntities extends State
  case object InsertDb extends State

  sealed trait Data{
    def inputs:Inputs
  }
  case object NoData extends Data{
    def inputs = Inputs(ActorRef.noSender, null)
  }
  case class Inputs(originator:ActorRef, request: CreateAccountCommand)
  trait InputsData extends Data {
    def inputs:Inputs
    def originator: ActorRef = inputs.originator
  }
  case class UnresolvedDependencies(inputs:Inputs) extends InputsData
  case class ResolvedDependencies(inputs:Inputs) extends InputsData
  case class LookedUpData(inputs:Inputs, user:AccountFO) extends InputsData

  object ResolutionIdent extends Enumeration {
    val Account: Value = Value
  }

  val InvalidUserIdError = ErrorMessage("user.invalid.userId", Some("You have supplied an invalid user id"))
  val RejectedUserError = ErrorMessage("user.invalid.rejected", Some("The user already exists"))

}

private[accounts] class AccountCreateValidator() extends FSM[AccountCreateValidator.State, AccountCreateValidator.Data] with AccountFactory {
  import AccountCreateValidator._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateAccountCommand, _) =>
      findingById ! FindAccountByUsernameCommand(request.entityId, request.vo.username)
      goto(LookingUpEntities) using ResolvedDependencies(Inputs(sender(), request))
  }

  when(LookingUpEntities, 5 seconds){
    case Event(FullResult(a: AccountFO), data: ResolvedDependencies) =>
      log.error("the account has been created before")
      data.originator ! Failure(FailureType.Validation, RejectedUserError)
      stop
    case Event(EmptyResult, ResolvedDependencies(inputs)) =>
      creator ! inputs.request
      goto(InsertDb) using LookedUpData(inputs, inputs.request.vo)
  }

  when(InsertDb, 10 seconds) {
    case Event(FullResult(txn: AccountFO), data: LookedUpData) =>
      context.parent.tell(CreateValidatedAccount(data.inputs.request), data.inputs.originator)
      stop
    case Event(failure: Failure, data: LookedUpData) =>
      data.originator ! failure
      stop
    case Event(empty: Empty, data: LookedUpData) =>
      data.originator ! empty
      stop
  }

  whenUnhandled {
    case Event(StateTimeout , data) =>
      log.error("Received state timeout in process to validate an order create request")
      data.inputs.originator ! unexpectedFail
      stop

    case Event(other, data) =>
      log.error("Received unexpected message of {} in state {}", other, stateName)
      data.inputs.originator ! unexpectedFail
      stop
  }

  def unexpectedFail = Failure(FailureType.Service, ServiceResult.UnexpectedFailure )

}