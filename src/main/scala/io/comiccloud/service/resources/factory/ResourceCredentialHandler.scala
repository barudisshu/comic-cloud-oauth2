package io.comiccloud.service.resources.factory

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.modeling.entity.{Account, Client, Token}
import io.comiccloud.rest._
import io.comiccloud.service.resources.ResourceEntity._
import io.comiccloud.service.resources._

import scala.concurrent.duration._
import scala.language.postfixOps

private[resources] object ResourceCredentialHandler {
  def props(): Props = Props(new ResourceCredentialHandler())

  sealed trait State
  case object WaitingForRequest extends State
  case object TokenMaintain     extends State
  case object AccountMaintain   extends State
  case object ClientMaintain    extends State

  sealed trait Data {
    def inputs: Inputs
  }

  case object NoData extends Data {
    override def inputs = Inputs(ActorRef.noSender, null)
  }

  case class Inputs(originator: ActorRef,
                    request: CredentialsDeliverCommand,
                    token: Token = null,
                    account: Account = null,
                    client: Client = null)

  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }

  case class UnresolvedDependencies(inputs: Inputs)           extends InputsData
  case class ResolvedDependencies(inputs: Inputs)             extends InputsData
  case class LookedUpData(inputs: Inputs, client: ResourceFO) extends InputsData

  val InvalidTokenIdError   = ErrorMessage("token.invalid.account", Some("the access_token does not found"))
  val InvalidAccountIdError = ErrorMessage("account.invalid.accountId", Some("the account_id does not found"))
  val InvalidClientIdError  = ErrorMessage("client.invalid.clientId", Some("the client_id does not found"))
}

class ResourceCredentialHandler()
    extends FSM[ResourceCredentialHandler.State, ResourceCredentialHandler.Data]
    with ResourceFactory {
  import ResourceCredentialHandler._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CredentialsDeliverCommand, _) =>
      findingByTokenId ! FindResourceRelateTokenIdCommand(request.vo.id)
      goto(TokenMaintain) using ResolvedDependencies(Inputs(sender(), request))
  }

  when(TokenMaintain, 5 seconds) {
    case Event(FullResult(token: Token), data @ ResolvedDependencies(inputs)) =>
      findingByAccountId ! FindResourceRelateAccountIdCommand(token.account_id.toString)
      goto(AccountMaintain) using data.copy(inputs = inputs.copy(token = token))
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("access token does not exists")
      context.parent.tell(HandleResourceTokenMissing, data.inputs.originator)
      stop
  }

  when(AccountMaintain, 5 seconds) {
    case Event(FullResult(account: Account), data @ ResolvedDependencies(inputs)) =>
      findingByClientId ! FindResourceRelateClientIdCommand(inputs.token.appid.toString)
      goto(ClientMaintain) using data.copy(inputs = inputs.copy(account = account))
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("account does not exists")
      context.parent.tell(HandleResourceAccountMissing, data.inputs.originator)
      stop
  }

  when(ClientMaintain, 5 seconds) {
    case Event(FullResult(client: Client), data @ ResolvedDependencies(inputs)) =>
      data.copy(inputs = inputs.copy(client = client))
      context.parent
        .tell(HandleResourceInfo(data.inputs.token, data.inputs.account, data.inputs.client), data.inputs.originator)
      stop
    case Event(EmptyResult, data: ResolvedDependencies) =>
      context.parent.tell(HandleResourceClientMissing, data.inputs.originator)
      data.originator ! Failure(FailureType.Service, InvalidTokenIdError)
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
