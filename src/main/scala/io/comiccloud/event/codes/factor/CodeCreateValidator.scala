package io.comiccloud.event.codes.factor

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.event.codes.CodeEntity.CreateValidatedCode
import io.comiccloud.event.codes.{CodeFO, CodeFactory, CreateCodeCommand, FindCodeByAccountIdCommand}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest._

import scala.concurrent.duration._
import scala.language.postfixOps

private[codes] object CodeCreateValidator {
  def props(clientsRepo: ClientsRepository, accountsRepo: AccountsRepository): Props =
    Props(new CodeCreateValidator(clientsRepo, accountsRepo))

  sealed trait State
  case object WaitingForRequest extends State
  case object CodeHasRespondedAccount extends State
  case object CodeHasRespondedClient extends State
  case object PersistenceRecord extends State

  sealed trait Data {
    def inputs: Inputs
  }

  case object NoData extends Data {
    override def inputs = Inputs(ActorRef.noSender, null)
  }

  case class Inputs(originator: ActorRef, request: CreateCodeCommand)

  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }

  case class UnresolvedDependencies(inputs: Inputs) extends InputsData
  case class ResolvedDependencies(inputs: Inputs) extends InputsData
  case class LookedUpData(inputs: Inputs, client: CodeFO) extends InputsData

  val InvalidAccountIdError = ErrorMessage("account.invalid.accountId", Some("the accountUid does not found"))
  val InvalidClientIdError = ErrorMessage("client.invalid.clientId", Some("the clientUid does not found"))
}

/**
  * checkout the generation, if pass, feedback CodeReadyFO
  */
private[codes] class CodeCreateValidator(
                                          val clientsRepo: ClientsRepository,
                                          val accountsRepo: AccountsRepository) extends FSM[CodeCreateValidator
.State, CodeCreateValidator.Data] with CodeFactory {

  import CodeCreateValidator._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateCodeCommand, _) =>
      findingByAccountId ! FindCodeByAccountIdCommand(request.vo.accountUid)
      goto(CodeHasRespondedAccount) using ResolvedDependencies(Inputs(sender(), request))
  }

  when(CodeHasRespondedAccount, 5 seconds)(transform{
    case Event(FullResult(_), data: ResolvedDependencies) =>
      stay using data
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the account")
      data.originator ! Failure(FailureType.Validation, InvalidAccountIdError)
      stop
  } using {
    case FSM.State(state, data, _, _, _) =>
      goto(CodeHasRespondedClient) using data
  })

  when(CodeHasRespondedClient, 5 seconds)(transform{
    case Event(FullResult(_), data: ResolvedDependencies) =>
      stay using data
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the client")
      data.originator ! Failure(FailureType.Validation, InvalidClientIdError)
      stop
  } using {
    case FSM.State(state, ResolvedDependencies(inputs), _, _, _) =>
      creator ! inputs.request
      goto(PersistenceRecord) using LookedUpData(inputs, inputs.request.vo)
  })

  when(PersistenceRecord, 10 seconds) {
    case Event(FullResult(txn: CodeFO), data: LookedUpData) =>
      context.parent.tell(CreateValidatedCode(data.inputs.request), data.inputs.originator)
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
