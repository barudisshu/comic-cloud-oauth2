package io.comiccloud.service.accounts.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.modeling.database.AccountDatabase
import io.comiccloud.modeling.entity.Account
import io.comiccloud.rest._
import io.comiccloud.service.accounts.{AccountFO, CreateAccountCommand}

object AccountCreator {
  def props(): Props = Props(new AccountCreator())
}

class AccountCreator() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case CreateAccountCommand(vo) =>
      val account = Account(
        id = UUID.fromString(vo.id),
        username = vo.username,
        password = vo.password,
        salt = vo.salt,
        email = vo.email,
        phone = vo.phone,
        created_at = vo.createdAt
      )
      context become feedback(vo, sender)
      AccountDatabase.saveOrUpdate(account).map(rs => if (rs.isFullyFetched()) account else None) pipeTo self
  }

  def feedback(o: AccountFO, replyTo: ActorRef): Receive = {
    case acc: Account =>
      replyTo ! FullResult(o)
      self ! PoisonPill
    case f: akka.actor.Status.Failure =>
      replyTo ! Failure(FailureType.Service, ErrorMessage("500", Some(s"db exception: ${f.cause.getLocalizedMessage}")))
      self ! PoisonPill
    case _ =>
      replyTo ! EmptyResult
      context stop self
  }
}
