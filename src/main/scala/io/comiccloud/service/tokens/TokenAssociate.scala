package io.comiccloud.service.tokens

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.tokens.{CreateClientCredentialTokenCommand, TokenEntity, TokenState}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

object TokenAssociate {
  val Name = "token-associate"
  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new TokenAssociate(accountsRepo, clientsRepo))
}

class TokenAssociate(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository) extends Aggregate[TokenState, TokenEntity] {

  override def entityProps: Props = TokenEntity.props(accountsRepo, clientsRepo)

  override def receive: Receive = {
    case command: CreateClientCredentialTokenCommand =>
      forwardCommand(command)
  }
}
