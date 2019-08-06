package io.comiccloud.service.tokens

import akka.actor._
import io.comiccloud.entity._

import scala.language.postfixOps

object TokenEntity {
  val Name           = "token"
  def props(): Props = Props(new TokenEntity())

  case class CreateValidatedToken(vo: TokenFO)
}

class TokenEntity() extends PersistentEntity[TokenState] with TokenFactory {

  override def initialState: TokenState = TokenInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CreateClientCredentialTokenCommand =>
      clientCredential.forward(o)
      state = CreatedValidationFO.validation

    case o: CreateValidatedClientCredentialTokenCommand =>
      clientCreator.forward(o)

  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case cmd: CreateClientCredentialTokenCommand  => true
    case cmd: CreateAuthorizationCodeTokenCommand => true
    case cmd: CreatePasswordTokenCommand          => true
    case cmd: CreateRefreshTokenCommand           => true
    case _                                        => false
  }
}
