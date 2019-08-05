package io.comiccloud.service.accounts

import akka.actor.Props
import io.comiccloud.entity.PersistentEntity
import io.comiccloud.service.accounts.AccountEntity.{CreateAccount, CreateValidatedAccount}

object AccountEntity {
  val Name           = "account"
  def props(): Props = Props(new AccountEntity())

  case class CreateValidatedAccount(cac: CreateAccountCommand)
  case class CreateAccount(cac: CreateAccountCommand)
  case class FindAccountById(cac: FindAccountByIdCommand)
}

class AccountEntity() extends PersistentEntity[AccountState] with AccountFactory {

  override def initialState: AccountState = AccountInitialState.empty

  override def additionalCommandHandling: Receive = {

    case cmd: CreateAccountCommand =>
      validator.forward(cmd)
      state = ValidationFO.validation

    case cmd: FindAccountByIdCommand =>
      findingById.forward(cmd)

    case CreateAccount(cmd) =>
      creator.forward(cmd)

    // if validate and create success, response the correct account
    case CreateValidatedAccount(cmd) =>
      state = cmd.vo
      handleResponse()
  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case _: CreateAccountCommand => true
    case _                       => false
  }
}
