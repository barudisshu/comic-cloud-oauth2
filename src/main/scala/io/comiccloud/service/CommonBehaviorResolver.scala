package io.comiccloud.service

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import io.comiccloud.modeling.entity.{Account, Client}
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.accounts.AccountFO
import io.comiccloud.service.clients.ClientFO

/**
  * the resolver one-to-one correspondence the cassandra query modeling
  *
  * 数据状态行为要和cassandra的建模一一对应
  *
  */
trait CommonBehaviorResolver {
  this: Actor with ActorLogging =>

  private def resolveUnhandled(replyTo: ActorRef): Receive = {
    case f: akka.actor.Status.Failure =>
      log.debug("{}", f.cause.getMessage)
      replyTo ! EmptyResult
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self
  }

  def resolveFindingAccountById(replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case Some(account: Account) =>
        val accountFO = AccountFO(
          id = account.id.toString,
          username = account.username,
          password = account.password,
          salt = account.salt,
          email = account.email,
          phone = account.phone,
          createdAt = account.created_at
        )
        replyTo ! FullResult(accountFO)
        self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)
  }

  def resolveFindingClientById(replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case Some(client: Client) =>
        val clientFO = ClientFO(
          id = client.appid.toString,
          ownerId = client.owner_id.toString,
          appid = client.appid.toString,
          appkey = client.appkey.toString,
          redirectUri = client.redirect_uri,
          grantType = client.grant_type,
          createdAt = client.created_at
        )
        replyTo ! FullResult(clientFO)
        self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)
  }

}
