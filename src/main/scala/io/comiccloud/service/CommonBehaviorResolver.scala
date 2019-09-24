package io.comiccloud.service

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import io.comiccloud.models.{Account, Client}
import io.comiccloud.rest._
import io.comiccloud.service.accounts.response.AccountResp
import io.comiccloud.service.clients.response.ClientResp

trait CommonBehaviorResolver {
  this: Actor with ActorLogging =>

  private[this] def resolveUnhandled(replyTo: ActorRef): Receive = {
    case f: akka.actor.Status.Failure =>
      log.debug("{}", f.cause.getMessage)
      replyTo ! Failure(FailureType.Service, ErrorMessage("401", Some(s"db exception: ${f.cause.getLocalizedMessage}")))
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self
    case it =>
      replyTo ! Failure(FailureType.Service, ErrorMessage("500", Some(s"unhandled receive message: $it")))
      context stop self
  }

  def resolveFindingAccountById(replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case Some(account: Account) =>
        val accountFO = AccountResp(
          id = account.uid,
          username = account.username,
          password = account.password,
          salt = account.salt,
          email = account.email,
          phone = account.phone,
          createdAt = account.createdAt
        )
        replyTo ! FullResult(accountFO)
        self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)
  }

  def resolveFindingClientById(replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case Some(client: Client) =>
        val clientFO = ClientResp(
          id = client.clientId,
          ownerId = client.ownerId,
          clientId = client.clientId,
          clientSecret = client.clientSecret,
          redirectUri = client.redirectUri,
          grantType = client.grantType,
          createdAt = client.createdAt
        )
        replyTo ! FullResult(clientFO)
        self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)
  }

  def feedback[U](o: Any, replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case _: U @unchecked =>
        replyTo ! FullResult(o)
        self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)
  }

}
