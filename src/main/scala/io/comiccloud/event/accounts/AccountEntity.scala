package io.comiccloud.event.accounts

import java.util.Date

import akka.actor._
import io.comiccloud.entity._
import io.comiccloud.repository._
import io.comiccloud.rest._
import io.comiccloud.service.accounts.AccountAssociate

import scala.concurrent.duration._
import scala.language.postfixOps

object AccountEntity {
  val Name = "account"
  def props(repo: AccountsRepository): Props = Props(new AccountEntity(repo))

  case class CreateValidatedAccount(cac: CreateAccountCommand)
  case class CreateAccount(cac: CreateAccountCommand)
  case class FindAccountById(cac: FindAccountByIdCommand)
}

class AccountEntity(val repo: AccountsRepository) extends PersistentEntity[AccountState] with AccountFactory {

  import AccountEntity._

  override def initialState: AccountState = AccountInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CreateAccountCommand =>
      // Kick off the validation process
      val validator = context.actorOf(AccountCreateValidator.props(repo))
      validator.forward(o)
      state = ValidationFO.validation

    case CreateValidatedAccount(cmd) =>
      // After insert into db, handle the event update the state, we can persist the complete account
      val state = cmd.vo
      persistAsync(AccountCreatedEvent(state))(handleEventAndRespond())


    // ========================================================================
    // atomicity operator show as below
    // ========================================================================

    case cmd: FindAccountByIdCommand =>
      finding.forward(cmd)

    case CreateAccount(cmd) =>
      creation.forward(cmd)
  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case ca:CreateAccountCommand => true
    case cva: CreateValidatedAccount => true
    case _ => false
  }

  override def handleEvent(event: EntityEvent): Unit = event match {
    case AccountCreatedEvent(accountFO) =>
      state = accountFO
  }
}

