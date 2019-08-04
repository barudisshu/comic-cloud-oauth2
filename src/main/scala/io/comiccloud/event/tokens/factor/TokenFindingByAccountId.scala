package io.comiccloud.event.tokens.factor

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.event.tokens.CreateClientCredentialTokenCommand
import io.comiccloud.repository.AccountsRepository

private[tokens] object TokenFindingByAccountId {
  def props(repo: AccountsRepository): Props = Props(new TokenFindingByAccountId(repo))
}

class TokenFindingByAccountId(repo: AccountsRepository) extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case ccctc: CreateClientCredentialTokenCommand =>

  }
}
