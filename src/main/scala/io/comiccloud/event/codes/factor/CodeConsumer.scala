package io.comiccloud.event.codes.factor

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.entity.PersistentEntity.GetState
import io.comiccloud.event.codes.CodeEntity.FindCodeById
import io.comiccloud.event.codes.{CodeFO, FindCodeByIdCommand}
import io.comiccloud.rest._

private[codes] object CodeConsumer {
  def props(): Props = Props(new CodeConsumer())

  sealed trait State
  case object WaitingForRequest extends State
  case object ResponseState extends State

  sealed trait Data {
    def inputs: Inputs
  }

  case object NoData extends Data {
    override def inputs = Inputs(ActorRef.noSender, null)
  }

  case class Inputs(originator: ActorRef, request: FindCodeByIdCommand)

  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }

  case class UnresolvedDependencies(inputs: Inputs) extends InputsData
  case class ResolvedDependencies(inputs: Inputs) extends InputsData
  case class LookedUpData(inputs: Inputs, client: CodeFO) extends InputsData

}

private[codes] class CodeConsumer() extends FSM[CodeConsumer.State, CodeConsumer.Data] {

  import CodeConsumer._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: FindCodeByIdCommand, _) =>
      context.parent.tell(GetState(request.entityId), self)
      goto(ResponseState) using ResolvedDependencies(Inputs(sender(), request))
  }

  when(ResponseState) {
    case Event(FullResult(_: CodeFO), data@ResolvedDependencies(inputs)) =>
      context.parent.tell(FindCodeById(inputs.request), data.inputs.originator)
      stop
    case Event(FullResult(_), data@ResolvedDependencies(_)) =>
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
