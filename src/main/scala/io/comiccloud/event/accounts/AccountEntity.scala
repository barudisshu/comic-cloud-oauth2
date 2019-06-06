package io.comiccloud.event.accounts

import akka.actor._
import io.comiccloud.entity._
import io.comiccloud.repository._

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
      validator.forward(o)
      state = ValidationFO.validation

    case CreateValidatedAccount(cmd) =>
      val state = cmd.vo
      persistAsync(AccountCreatedEvent(state))(handleEventAndRespond())

    case cmd: FindAccountByIdCommand =>
      findingById.forward(cmd)

    case CreateAccount(cmd) =>
      creator.forward(cmd)
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

