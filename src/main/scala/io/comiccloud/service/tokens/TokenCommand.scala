package io.comiccloud.service.tokens

import io.comiccloud.entity.EntityCommand
import io.comiccloud.service.clients.ClientFO

trait TokenCommand extends EntityCommand

case class CreateClientCredentialsTokenCommand(vo: TokenClientCredentialsFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class CreateAuthorizationCodeTokenCommand(vo: TokenAuthorizationCodeFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class CreatePasswordTokenCommand(vo: TokenPasswordFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class CreateRefreshTokenCommand(vo: TokenRefreshTokenFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class CreateValidatedTokenCommand(vo: TokenFO) extends TokenCommand {
  override def entityId: String = vo.id
}

case class FindTokenRelateAccountIdCommand(accountId: String) extends TokenCommand {
  override def entityId: String = accountId
}

case class FindTokenRelateClientCommand(clientId: String, clientSecret: String) extends TokenCommand {
  override def entityId: String = clientId
}

case class FindTokenRelateCodeCommand(codeId: String) extends TokenCommand {
  override def entityId: String = codeId
}

case class DeleteTokenRelateCodeCommand(codeId: String) extends TokenCommand {
  override def entityId: String = codeId
}

case class FindTokenRelateRefreshCommand(clientFO: ClientFO, refreshToken: String) extends TokenCommand {
  override def entityId: String = refreshToken
}

case class DeleteTokenRelateAccessCommand(accessToken: String) extends TokenCommand {
  override def entityId: String = accessToken
}
