package io.comiccloud.service.codes

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.codes._
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

object CodeAssociate {
  val Name = "code-associate"
  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new CodeAssociate(accountsRepo, clientsRepo))
}

class CodeAssociate(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository) extends Aggregate[CodeState, CodeEntity] {

  override def entityProps: Props = CodeEntity.props(accountsRepo, clientsRepo)
  override def receive: Receive = {
    case command: CreateCodeCommand =>
      forwardCommand(command)
    case command: FindCodeRelateAccountIdCommand =>
      forwardCommand(command)
    case command: FindCodeRelateClientIdCommand =>
      forwardCommand(command)
    case command: FindCodeByIdCommand =>
      forwardCommand(command)
  }
}
