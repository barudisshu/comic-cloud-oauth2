package io.comiccloud.service.codes

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.codes._
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

object CodeAssociate {
  val Name = "code-associate"
  def props(clientsRepo: ClientsRepository, accountsRepo: AccountsRepository): Props =
    Props(new CodeAssociate(clientsRepo, accountsRepo))
}

class CodeAssociate(clientsRepo: ClientsRepository, accountsRepo: AccountsRepository) extends Aggregate[CodeState, CodeEntity] {

  override def entityProps: Props = CodeEntity.props(clientsRepo, accountsRepo)
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
