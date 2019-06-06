package io.comiccloud.event.tokens

import io.comiccloud.entity.EntityCommand

trait TokenCommand extends EntityCommand

case class CreateClientCredentialTokenCommand(vo: TokenFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class CreateValidatedClientCredentialTokenCommand(vo: TokenFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class CreateAuthorizationCodeTokenCommand(vo: TokenFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class CreatePasswordTokenCommand(vo: TokenFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class CreateRefreshTokenCommand(vo: TokenFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class FindTokenRelateAccountIdCommand(accountId: String) extends TokenCommand {
  override def entityId: String = accountId
}

case class FindTokenRelateClientIdCommand(clientId: String) extends TokenCommand {
  override def entityId: String = clientId
}