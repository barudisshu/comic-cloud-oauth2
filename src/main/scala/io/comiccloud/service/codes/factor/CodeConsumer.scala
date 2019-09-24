package io.comiccloud.service.codes.factor

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.rest._
import io.comiccloud.service.codes.CodeActor.FindCodeById
import io.comiccloud.service.codes.request.FindCodeByIdReq
import io.comiccloud.service.codes.response.CodeResp

private[codes] object CodeConsumer {
  def props(): Props = Props(new CodeConsumer())

  sealed trait State
  case object WaitingForRequest extends State
  case object ResponseState     extends State

  sealed trait Data {
    def inputs: Inputs
  }

  case object NoData extends Data {
    override def inputs = Inputs(ActorRef.noSender, null)
  }

  case class Inputs(originator: ActorRef, request: FindCodeByIdReq)

  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }

  case class UnresolvedDependencies(inputs: Inputs)         extends InputsData
  case class ResolvedDependencies(inputs: Inputs)           extends InputsData
  case class LookedUpData(inputs: Inputs, client: CodeResp) extends InputsData

}

private[codes] class CodeConsumer() extends FSM[CodeConsumer.State, CodeConsumer.Data] {

  import CodeConsumer._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: FindCodeByIdReq, _) =>
      context.parent.tell(FindCodeById, self)
      goto(ResponseState) using ResolvedDependencies(Inputs(sender(), request))
  }

  when(ResponseState) {
    case Event(FullResult(_: CodeResp), data @ ResolvedDependencies(_)) =>
      context.parent.tell(FindCodeById, data.inputs.originator)
      stop
    case Event(EmptyResult, data @ ResolvedDependencies(_)) =>
      data.originator ! EmptyResult
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
