package io.comiccloud.service.tokens.factory.credentials

import akka.actor.{ActorRef, FSM, Props}
import io.comiccloud.digest.Hashes
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest._
import io.comiccloud.service.accounts.response.AccountResp
import io.comiccloud.service.clients.response.ClientResp
import io.comiccloud.service.tokens.TokenActor.CreateValidatedToken
import io.comiccloud.service.tokens._
import io.comiccloud.service.tokens.request.{CreateClientCredentialsTokenReq, CreateTokenReq, FindTokenRelateAccountIdReq, FindTokenRelateClientReq, FindTokenRelateCodeReq}
import io.comiccloud.service.tokens.response.TokenResp

import scala.concurrent.duration._
import scala.language.postfixOps

object TokenClientCredentialsCreateValidator {
  def props(accountRepo: AccountsRepository,
            clientRepo: ClientsRepository,
            codeRef: ActorRef): Props =
    Props(new TokenClientCredentialsCreateValidator(accountRepo, clientRepo, codeRef))

  sealed trait State
  case object WaitingForRequest        extends State
  case object TokenHasRespondedAccount extends State
  case object TokenHasRespondedSubject extends State
  case object LookingUpEntities        extends State
  case object InsertDb                 extends State

  sealed trait Data {
    def inputs: Inputs
  }
  case object NoData extends Data {
    def inputs = Inputs(ActorRef.noSender, null)
  }
  case class Inputs(originator: ActorRef, request: CreateClientCredentialsTokenReq)
  trait InputsData extends Data {
    def inputs: Inputs
    def originator: ActorRef = inputs.originator
  }
  case class UnresolvedDependencies(inputs: Inputs)      extends InputsData
  case class ResolvedDependencies(inputs: Inputs)        extends InputsData
  case class LookedUpData(inputs: Inputs,
                          clientResp: ClientResp,
                          accountResp: AccountResp,
                          token: TokenResp) extends InputsData

  object ResolutionIdent extends Enumeration {
    val Token: Value = Value
  }

  val InvalidClientIdError = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))
  val InvalidAccountIdError = ErrorMessage("account.invalid.accountId", Some("You have supplied an invalid account id"))

}

private[tokens] class TokenClientCredentialsCreateValidator(val accountRepo: AccountsRepository,
                                                            val clientRepo: ClientsRepository,
                                                            val codeRef: ActorRef)
    extends FSM[TokenClientCredentialsCreateValidator.State, TokenClientCredentialsCreateValidator.Data]
    with TokenFactory {

  import TokenClientCredentialsCreateValidator._

  startWith(WaitingForRequest, NoData)

  when(WaitingForRequest) {
    case Event(request: CreateClientCredentialsTokenReq, _) =>
      findingByClientId ! FindTokenRelateClientReq(request.vo.clientId, request.vo.clientSecret)
      goto(TokenHasRespondedAccount) using ResolvedDependencies(Inputs(sender, request))
  }

  when(TokenHasRespondedAccount, 5 seconds) {
    case Event(FullResult(clientResp: ClientResp), ResolvedDependencies(inputs)) =>
      findingByAccountId ! FindTokenRelateAccountIdReq(clientResp.ownerId)
      goto(TokenHasRespondedSubject) using LookedUpData(inputs, clientResp, null, null)
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the client")
      data.originator ! Failure(FailureType.Validation, InvalidClientIdError)
      stop
  }

  when(TokenHasRespondedSubject, 5 seconds) {
    case Event(FullResult(accountResp: AccountResp), LookedUpData(inputs, clientResp, _, _)) =>
      val tokenResp = TokenResp(
        accountId = clientResp.ownerId,
        clientId = inputs.request.vo.clientId,
        accessToken = Hashes.randomSha256().toString,
        refreshToken = Hashes.randomSha256().toString,
        account = accountResp,
        client = clientResp
      )
      creator ! CreateTokenReq(tokenResp)
      goto(InsertDb) using LookedUpData(inputs, clientResp, accountResp, tokenResp)
    case Event(EmptyResult, data: ResolvedDependencies) =>
      log.error("can not find the account")
      data.originator ! Failure(FailureType.Validation, InvalidAccountIdError)
      stop
  }

  when(InsertDb, 10 seconds) {
    case Event(FullResult(txn: TokenResp), LookedUpData(inputs, _, _, tokenResp)) =>
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
