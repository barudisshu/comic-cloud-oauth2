package io.comiccloud.service.tokens.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.modeling.database.TokenDatabase
import io.comiccloud.modeling.entity.Token
import io.comiccloud.rest._
import io.comiccloud.service.tokens.{CreateValidatedClientCredentialTokenCommand, TokenPair}

private[tokens] object TokenClientCredentialCreator {
  def props(): Props = Props(new TokenClientCredentialCreator())
}

class TokenClientCredentialCreator() extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case CreateValidatedClientCredentialTokenCommand(vo) =>
      val token = Token(
        account_id = UUID.fromString(vo.accountId),
        appid = UUID.fromString(vo.appid),
        access_token = vo.token,
        refresh_token = vo.refreshToken,
        created_at = vo.createdAt
      )

      context become feedback(sender)
      TokenDatabase.saveOrUpdate(token).map(rs => if (rs.isFullyFetched()) token else None) pipeTo self
  }

  def feedback(replyTo: ActorRef): Receive = {
    case token: Token =>
      replyTo ! FullResult(TokenPair(token.access_token, token.refresh_token))
      self ! PoisonPill

    case f: akka.actor.Status.Failure =>
      replyTo ! Failure(FailureType.Service, ErrorMessage("500", Some(s"db exception: ${f.cause.getLocalizedMessage}")))
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self

  }
}
