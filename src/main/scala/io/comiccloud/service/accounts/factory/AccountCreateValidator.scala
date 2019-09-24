package io.comiccloud.service.accounts.factory

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.rest.{Empty, EmptyResult, ErrorMessage, Failure, FailureType, FullResult, ServiceResult}
import io.comiccloud.service.accounts.AccountActor.CreateValidatedAccount
import io.comiccloud.service.accounts.AccountFactory
import io.comiccloud.service.accounts.request.{CreateAccountReq, FindAccountByUsernameReq}
import io.comiccloud.service.accounts.response.AccountResp

import scala.concurrent.duration._
import scala.language.postfixOps

private[accounts] object AccountCreateValidator {
  def props(accountRepo: AccountsRepository) = Props(new AccountCreateValidator(accountRepo))

  sealed trait State
  case object WaitingForRequest extends State
  case object LookingUpEntities extends State
  case object InsertDb          extends State

  sealed trait Data {
    def inputs: Inputs
  }
  case object NoData extends Data {
    def inputs = Inputs(ActorRef.noSender, null)
  }
  case class Inputs(originator: ActorRef, request: CreateAccountReq)
  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }
  case class UnresolvedDependencies(inputs: Inputs)        extends InputsData
  case class ResolvedDependencies(inputs: Inputs)          extends InputsData
  case class LookedUpData(inputs: Inputs, user: AccountResp) extends InputsData

  object ResolutionIdent extends Enumeration {
    val Account: Value = Value
  }

  val InvalidUserIdError = ErrorMessage("user.invalid.userId", Some("You have supplied an invalid user id"))
  val RejectedUserError  = ErrorMessage("user.invalid.rejected", Some("The user already exists"))
  val DatabaseError      = ErrorMessage("database.exception.occur", Some("Service error occur"))

}

private[accounts] class AccountCreateValidator(val accountRepo: AccountsRepository)
    extends FSM[AccountCreateValidator.State, AccountCreateValidator.Data]
    with AccountFactory {
  import AccountCreateValidator._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateAccountReq, _) =>
      findingById ! FindAccountByUsernameReq(request.vo.username)
      goto(LookingUpEntities) using ResolvedDependencies(Inputs(sender(), request))
  }

  when(LookingUpEntities, 5 seconds) {
    case Event(FullResult(_: AccountResp), data: ResolvedDependencies) =>
      log.error("the account has been created before")
      data.originator ! Failure(FailureType.Validation, RejectedUserError)
      stop
    case Event(EmptyResult, ResolvedDependencies(inputs)) =>
      creator ! inputs.request
      goto(InsertDb) using LookedUpData(inputs, inputs.request.vo)
    case Event(failure: Failure, ResolvedDependencies(inputs)) =>
      log.error("db exception")
      inputs.originator ! failure
      stop
  }

  when(InsertDb, 5 seconds) {
    case Event(FullResult(_: AccountResp), data: LookedUpData) =>
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
