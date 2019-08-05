package io.comiccloud.event.accounts

import akka.actor._
import io.comiccloud.repository._
import io.comiccloud.rest.FullResult

import scala.language.postfixOps

object AccountEntity {
  val Name = "account"
  def props(repo: AccountsRepository): Props = Props(new AccountEntity(repo))

  case class CreateValidatedAccount(cac: CreateAccountCommand)
  case class CreateAccount(cac: CreateAccountCommand)
  case class FindAccountById(cac: FindAccountByIdCommand)
}

class AccountEntity(val repo: AccountsRepository) extends Actor with ActorLogging with AccountFactory {

  import AccountEntity._

  override def receive: Receive = {
    case o: CreateAccountCommand =>
      validator.forward(o)

    case CreateValidatedAccount(cmd) =>
      sender() ! FullResult(cmd.vo)

    case cmd: FindAccountByIdCommand =>
      findingById.forward(cmd)

    case CreateAccount(cmd) =>
      creator.forward(cmd)
  }

}

