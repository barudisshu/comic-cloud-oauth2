package io.comiccloud.service.tokens.factory.refresh_token

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, ReceiveTimeout}
import io.comiccloud.digest.Hashes
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest._
import io.comiccloud.service.clients.response.ClientResp
import io.comiccloud.service.tokens.TokenActor.CreateValidatedToken
import io.comiccloud.service.tokens.TokenFactory
import io.comiccloud.service.tokens.request.{CreateRefreshTokenReq, FindTokenRelateClientReq}
import io.comiccloud.service.tokens.response.{TokenRefreshTokenResp, TokenResp}
import io.comiccloud.util.ReplicatedCache
import io.comiccloud.util.ReplicatedCache.{Cached, Evict, GetFromCache, PutInCache}

import scala.concurrent.duration._
import scala.language.postfixOps

object TokenRefreshTokenCreateValidator {

  def props(accountRepo: AccountsRepository, clientRepo: ClientsRepository, codeRef: ActorRef): Props =
    Props(new TokenRefreshTokenCreateValidator(accountRepo, clientRepo, codeRef))

  val InvalidClientIdError = ErrorMessage("client.invalid.clientId", Some("You have supplied an invalid client id"))

}

/**
  * As the use has been verify before, only check the client exist or not
  */
class TokenRefreshTokenCreateValidator(val accountRepo: AccountsRepository,
                                       val clientRepo: ClientsRepository,
                                       val codeRef: ActorRef)
    extends Actor
    with ActorLogging
    with TokenFactory {
  context.setReceiveTimeout(10 seconds)
  import TokenRefreshTokenCreateValidator._
  val cache: ActorRef = context.actorOf(ReplicatedCache.props)

  override def receive: Receive = {
    case CreateRefreshTokenReq(vo) =>
      context become tokenHasRespondedAccount(vo, sender)
      findingByClientId ! FindTokenRelateClientReq(vo.clientId, vo.clientSecret)
  }

  def tokenHasRespondedAccount(vo: TokenRefreshTokenResp, replyTo: ActorRef): Receive = {
    case FullResult(_: ClientResp) =>
      context become reset(vo, replyTo)
      cache.tell(GetFromCache(s"#${vo.refreshToken}"), self)
    case EmptyResult =>
      replyTo ! Failure(FailureType.Validation, InvalidClientIdError)
      self ! PoisonPill
  }

  def reset(vo: TokenRefreshTokenResp, replyTo: ActorRef): Receive = {
    case Cached(_, Some(v)) =>
      v match {
        case tk: TokenResp =>
          cache ! Evict(s"@${tk.accessToken}")
          cache ! Evict(s"#${tk.refreshToken}")

          val accessToken  = Hashes.randomSha256().toString
          val refreshToken = Hashes.randomSha256().toString

          val retk = tk.copy(accessToken = accessToken).copy(refreshToken = refreshToken)

          cache ! PutInCache(s"@$accessToken", retk)
          cache ! PutInCache(s"#$refreshToken", retk)

          context.parent.tell(CreateValidatedToken(retk), replyTo)

        case _ =>
          replyTo ! Failure(FailureType.Validation,
                            ErrorMessage("token.invalid.refreshToken", Some("the refresh token is expired ")))
      }
    case Cached(key, None) =>
      replyTo ! Failure(FailureType.Validation,
                        ErrorMessage("token.invalid.refreshToken", Some("the refresh token is expired ")))

      self ! PoisonPill

    case ReceiveTimeout =>
      replyTo ! Failure(FailureType.Service, ServiceResult.UnexpectedFailure)
  }
}
