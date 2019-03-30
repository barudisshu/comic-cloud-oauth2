package io.comiccloud.event.accounts

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.models.Account
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.rest._

object AccountCreation {
  def props(repo: AccountsRepository): Props = Props(new AccountCreation(repo))
}

class AccountCreation(repo: AccountsRepository) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case CreateAccountCommand(vo) =>
      val account = Account(
        id = None,
        uid = vo.id,
        username = vo.username,
        password = vo.password,
        salt = vo.salt,
        email = vo.email,
        phone = vo.phone,
        createdAt = vo.createdAt
      )
      context become feedback(vo, sender)
      repo.insert(account) pipeTo self
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
