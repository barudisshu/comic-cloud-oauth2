package io.comiccloud.service.accounts

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.AccountsRepository
import io.comiccloud.rest.FullResult
import io.comiccloud.service.accounts.AccountActor.{CreateAccount, CreateValidatedAccount}
import io.comiccloud.service.accounts.request.{CreateAccountReq, FindAccountByIdReq}

object AccountActor {
  val Name = "account"
  def props(accountRepo: AccountsRepository): Props = Props(new AccountActor(accountRepo))

  case class CreateValidatedAccount(cac: CreateAccountReq)
  case class CreateAccount(cac: CreateAccountReq)
  case class FindAccountById(cac: FindAccountByIdReq)

}
class AccountActor(val accountRepo: AccountsRepository) extends Actor with ActorLogging with AccountFactory {
  override def receive: Receive = {
    case cmd: CreateAccountReq =>
      validator.forward(cmd)
    case cmd: FindAccountByIdReq =>
      findingById.forward(cmd)
    case CreateAccount(cmd) =>
      creator.forward(cmd)
    case CreateValidatedAccount(cmd) =>
      sender() ! FullResult(cmd.vo)
  }
}
