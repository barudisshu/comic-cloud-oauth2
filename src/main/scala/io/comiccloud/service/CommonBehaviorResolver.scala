package io.comiccloud.service

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.ResultSet
import io.comiccloud.modeling.entity.{Account, Client, Code, Token}
import io.comiccloud.rest._
import io.comiccloud.service.accounts.AccountFO
import io.comiccloud.service.clients.ClientFO
import io.comiccloud.service.codes.{CodeDeleteFO, CodeFO}
import io.comiccloud.service.tokens.{TokenDeleteFO, TokenFO}

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

  def resolveFindingCodeById(replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case Some(code: Code) =>
        val codeFO = CodeFO(
          id = code.code,
          accountId = code.account_id.toString,
          appid = code.appid.toString,
          redirectUri = code.redirect_uri,
          code = code.code,
          createdAt = code.created_at
        )
        replyTo ! FullResult(codeFO)
        self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)
  }

  def resolveFindingRefreshById(clientFO: ClientFO, replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case Some(code: Token) =>
        val id = UUIDs.timeBased().toString
        val tokenFO = TokenFO(
          id = id,
          accountId = code.account_id.toString,
          appid = clientFO.appid,
          appkey = clientFO.appkey,
          token = id,
          refreshToken = UUIDs.timeBased().toString
        )
        replyTo ! FullResult(tokenFO)
        self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)

  }

  def resolveDeletingCodeById(code: String, replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case rs: ResultSet if rs.isFullyFetched() && rs.isExhausted() =>
        replyTo ! FullResult(CodeDeleteFO(code))
        self ! PoisonPill
      case rs: ResultSet =>
        replyTo ! EmptyResult
      self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)

  }

  def resolveDeletingAccessToken(accessToken: String, replyTo: ActorRef): Receive = {
    val trans: Receive = {
      case rs: ResultSet if rs.isFullyFetched() && rs.isExhausted() =>
        replyTo ! FullResult(TokenDeleteFO(accessToken))
        self ! PoisonPill
      case rs: ResultSet =>
        replyTo ! EmptyResult
        self ! PoisonPill
    }
    trans orElse resolveUnhandled(replyTo)

  }

}
