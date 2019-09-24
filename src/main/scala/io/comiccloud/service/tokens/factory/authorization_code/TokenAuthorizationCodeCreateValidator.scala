package io.comiccloud.service.tokens.factory.authorization_code

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.digest.Hashes
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest._
import io.comiccloud.service.accounts.response.AccountResp
import io.comiccloud.service.clients.response.ClientResp
import io.comiccloud.service.codes.response.CodeResp
import io.comiccloud.service.tokens.TokenActor.CreateValidatedToken
import io.comiccloud.service.tokens._
import io.comiccloud.service.tokens.request._
import io.comiccloud.service.tokens.response.TokenResp

import scala.concurrent.duration._
import scala.language.postfixOps

object TokenAuthorizationCodeCreateValidator {
  def props(accountRepo: AccountsRepository,
            clientRepo: ClientsRepository,
            codeRef: ActorRef): Props =
    Props(new TokenAuthorizationCodeCreateValidator(accountRepo, clientRepo, codeRef))

  sealed trait State
  case object WaitingForRequest        extends State
  case object TokenHasRespondedAccount extends State
  case object TokenHasRespondedSubject extends State
  case object TokenHasRespondedCode    extends State
  case object PersistenceRecord extends State

  sealed trait Data {
    def inputs: Inputs
  }
  case object NoData extends Data {
    def inputs = Inputs(ActorRef.noSender, null)
  }
  case class Inputs(originator: ActorRef, request: CreateAuthorizationCodeTokenReq)
  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }
  case class UnresolvedDependencies(inputs: Inputs)                                              extends InputsData
  case class ResolvedDependencies(inputs: Inputs)                                                extends InputsData
  case class LookedUpData(inputs: Inputs,
                          clientResp: ClientResp,
                          accountResp: AccountResp,
                          codeResp: CodeResp,
                          user: TokenResp) extends InputsData

  object ResolutionIdent extends Enumeration {
    val Token: Value = Value
  }

  val InvalidClientIdError  = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))
  val InvalidAccountIdError = ErrorMessage("account.invalid.accountId", Some("You have supplied an invalid account id"))
  val InvalidCodeIdError    = ErrorMessage("code.invalid.codeId", Some("You have supplied an invalid code"))

}

private[tokens] class TokenAuthorizationCodeCreateValidator(val accountRepo: AccountsRepository,
                                                            val clientRepo: ClientsRepository,
                                                            val codeRef: ActorRef)
    extends FSM[TokenAuthorizationCodeCreateValidator.State, TokenAuthorizationCodeCreateValidator.Data]
    with TokenFactory {

  import TokenAuthorizationCodeCreateValidator._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateAuthorizationCodeTokenReq, _) =>
      findingByClientId ! FindTokenRelateClientReq(request.vo.clientId, request.vo.clientSecret)
      goto(TokenHasRespondedAccount) using ResolvedDependencies(Inputs(sender, request))
  }

  when(TokenHasRespondedAccount, 5 seconds) {
    case Event(FullResult(clientResp: ClientResp), ResolvedDependencies(inputs)) =>
      findingByAccountId ! FindTokenRelateAccountIdReq(clientResp.ownerId)
      goto(TokenHasRespondedSubject) using LookedUpData(inputs, clientResp, null, null, null)
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the client")
      data.originator ! Failure(FailureType.Validation, InvalidClientIdError)
      stop
  }

  when(TokenHasRespondedSubject, 5 seconds) {
    case Event(FullResult(accountResp: AccountResp), LookedUpData(inputs, clientResp, _, _, _)) =>
      findingByCodeId ! FindTokenRelateCodeReq(inputs.request.vo.code)
      goto(TokenHasRespondedCode) using LookedUpData(inputs, clientResp, accountResp, null, null)
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the account")
      data.originator ! Failure(FailureType.Validation, InvalidAccountIdError)
      stop
  }

  when(TokenHasRespondedCode, 5 seconds) {
    case Event(FullResult(codeResp: CodeResp), LookedUpData(inputs, clientResp, accountResp, _, _)) =>
      val tokenResp = TokenResp(
        accountId = clientResp.ownerId,
        clientId = inputs.request.vo.clientId,
        accessToken = Hashes.randomSha256().toString,
        refreshToken = Hashes.randomSha256().toString,
        account = accountResp,
        client = clientResp
      )
      creator ! CreateTokenReq(tokenResp)
      goto(PersistenceRecord) using LookedUpData(inputs, clientResp, accountResp, codeResp, tokenResp)
    case Event(EmptyResult, data: LookedUpData) =>
      log.error("can not find the code, may it's expired")
      data.originator ! Failure(FailureType.Validation, InvalidCodeIdError)
      stop
    case Event(_: Failure, data: LookedUpData) =>
      log.error("can not find the code, not found at all")
      data.originator ! Failure(FailureType.Validation, InvalidCodeIdError)
      stop
  }

  when(PersistenceRecord, 10 seconds) {
    case Event(FullResult(txn: TokenResp), LookedUpData(inputs, _, _, _, tokenResp)) =>
      context.parent.tell(CreateValidatedToken(tokenResp), inputs.originator)
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
