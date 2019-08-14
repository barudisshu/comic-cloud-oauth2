package io.comiccloud.service.tokens.factory.refresh_token

import akka.actor.{ActorRef, FSM, Props}
import com.datastax.driver.core.utils.UUIDs
import io.comiccloud.rest._
import io.comiccloud.service.clients.ClientFO
import io.comiccloud.service.tokens._

import scala.concurrent.duration._
import scala.language.postfixOps

object TokenRefreshTokenCreateValidator {
  def props(): Props = Props(new TokenRefreshTokenCreateValidator())

  sealed trait State
  case object WaitingForRequest        extends State
  case object TokenHasRespondedAccount extends State
  case object TokenHasRespondedRefresh extends State
  case object TokenAccessHasBeenDelete extends State
  case object InsertDb                 extends State

  sealed trait Data {
    def inputs: Inputs
  }
  case object NoData extends Data {
    def inputs = Inputs(ActorRef.noSender, null)
  }
  case class Inputs(originator: ActorRef, request: CreateRefreshTokenCommand)
  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }
  case class UnresolvedDependencies(inputs: Inputs)                             extends InputsData
  case class ResolvedDependencies(inputs: Inputs)                               extends InputsData
  case class LookedUpData(inputs: Inputs, clientFO: ClientFO, tokenFO: TokenFO) extends InputsData

  object ResolutionIdent extends Enumeration {
    val Token: Value = Value
  }

  val InvalidClientIdError = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))
  val InvalidAccessIdError = ErrorMessage("token.invalid.accessId", Some("You have supplied an invalid refresh token"))
  val InvalidRefreshIdError = ErrorMessage("token.invalid.refreshId", Some("refresh token does not exists, it may be auto delete by ttl"))

}

private[tokens] class TokenRefreshTokenCreateValidator
    extends FSM[TokenRefreshTokenCreateValidator.State, TokenRefreshTokenCreateValidator.Data]
    with TokenFactory {

  import TokenRefreshTokenCreateValidator._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateRefreshTokenCommand, _) =>
      findingByClientId ! FindTokenRelateClientCommand(request.vo.appid, request.vo.appkey)
      goto(TokenHasRespondedAccount) using ResolvedDependencies(Inputs(sender, request))
  }

  when(TokenHasRespondedAccount, 5 seconds) {
    case Event(FullResult(clientFO: ClientFO), data @ ResolvedDependencies(inputs)) =>
      log.debug("the client does exists {}", clientFO.ownerId)
      findingByRefreshToken ! FindTokenRelateRefreshCommand(clientFO, inputs.request.vo.refreshToken)
      goto(TokenHasRespondedRefresh) using LookedUpData(inputs, clientFO, null)
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the client")
      data.originator ! Failure(FailureType.Validation, InvalidClientIdError)
      stop
  }

  when(TokenHasRespondedRefresh, 5 seconds) {
    case Event(FullResult(tokenFO: TokenFO), data @ LookedUpData(inputs, clientFO, _)) =>
      deleteAccessTokenId ! DeleteTokenRelateAccessCommand(tokenFO.id)
      goto(TokenAccessHasBeenDelete) using data.copy(tokenFO = tokenFO)
    case Event(_:Failure, data: LookedUpData) =>
      log.error("can not find the refresh token")
      data.originator ! Failure(FailureType.Validation, InvalidAccessIdError)
      stop
    case Event(EmptyResult, data: LookedUpData) =>
      log.error("can not find the refresh token")
      data.originator ! Failure(FailureType.Validation, InvalidRefreshIdError)
      stop
  }

  when(TokenAccessHasBeenDelete, 5 seconds)(transform{
    case Event(FullResult(_: TokenDeleteFO), data: LookedUpData) =>
    stay.using(data)
    case Event(EmptyResult, data: LookedUpData) =>
    stay.using(data)
  }.using {
    case FSM.State(_, LookedUpData(inputs, clientFO, tokenFO), _, _, _) =>
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
