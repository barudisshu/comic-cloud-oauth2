package io.comiccloud.service.tokens.factory.authorization_code

import akka.actor.{ActorRef, FSM, Props}
import com.datastax.driver.core.utils.UUIDs
import io.comiccloud.rest._
import io.comiccloud.service.clients.ClientFO
import io.comiccloud.service.codes.{CodeDeleteFO, CodeFO}
import io.comiccloud.service.tokens._

import scala.concurrent.duration._
import scala.language.postfixOps

object TokenAuthorizationCodeCreateValidator {
  def props(): Props = Props(new TokenAuthorizationCodeCreateValidator())

  sealed trait State
  case object WaitingForRequest        extends State
  case object TokenHasRespondedAccount extends State
  case object TokenHasRespondedCode    extends State
  case object TokenCodeHasBeenDelete   extends State

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
  case class UnresolvedDependencies(inputs: Inputs)                                          extends InputsData
  case class ResolvedDependencies(inputs: Inputs)                                            extends InputsData
  case class LookedUpData(inputs: Inputs, clientFO: ClientFO, codeFO: CodeFO, user: TokenFO) extends InputsData

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

  when(WaitingForRequest) {
    case Event(request: CreateAuthorizationCodeTokenCommand, _) =>
      findingByClientId ! FindTokenRelateClientCommand(request.vo.appid, request.vo.appkey)
      goto(TokenHasRespondedAccount) using ResolvedDependencies(Inputs(sender, request))
  }

  when(TokenHasRespondedAccount, 5 seconds) {
    case Event(FullResult(clientFO: ClientFO), data @ ResolvedDependencies(inputs)) =>
      log.debug("the client does exists {}", clientFO.ownerId)
      findingByCodeId ! FindTokenRelateCodeCommand(inputs.request.vo.code)
      goto(TokenHasRespondedCode) using LookedUpData(inputs, clientFO, null, null)
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the client")
      data.originator ! Failure(FailureType.Validation, InvalidClientIdError)
      stop
  }

  when(TokenHasRespondedCode, 5 seconds) {
    case Event(FullResult(codeFO: CodeFO), data @ LookedUpData(inputs, _, _, _)) =>
      deletingByCodeId ! DeleteTokenRelateCodeCommand(inputs.request.vo.code)
      goto(TokenCodeHasBeenDelete) using data.copy(codeFO = codeFO)
    case Event(EmptyResult, data: LookedUpData) =>
      log.error("can not find the code, may it's expired")
      data.originator ! Failure(FailureType.Validation, InvalidCodeIdError)
      stop
    case Event(_:Failure, data: LookedUpData) =>
    log.error("can not find the code, not found at all")
      data.originator ! Failure(FailureType.Validation, InvalidCodeIdError)
      stop
  }

  when(TokenCodeHasBeenDelete, 5 seconds)(transform {
    case Event(FullResult(_: CodeDeleteFO), data: LookedUpData) =>
      stay.using(data)
    case Event(EmptyResult, data: LookedUpData) =>
      stay.using(data)
  }.using {
    case FSM.State(_, LookedUpData(inputs, clientFO, _, _), _, _, _) =>
      val tokenFO = TokenFO(
        id = inputs.request.vo.id,
        accountId = clientFO.ownerId,
        appid = inputs.request.vo.appid,
        appkey = inputs.request.vo.appkey,
        token = inputs.request.vo.id,
        refreshToken = UUIDs.timeBased().toString
      )
      context.parent.tell(CreateValidatedTokenCommand(tokenFO), inputs.originator)
      stop
  })

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
