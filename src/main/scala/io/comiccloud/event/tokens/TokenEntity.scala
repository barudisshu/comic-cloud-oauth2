package io.comiccloud.event.tokens

import akka.actor._
import akka.cluster.sharding.ShardRegion.Passivate
import io.comiccloud.entity.PersistentEntity.StopEntity
import io.comiccloud.entity._
import io.comiccloud.repository._

import scala.language.postfixOps

object TokenEntity {
  val Name = "token"
  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new TokenEntity(accountsRepo, clientsRepo))

  case class CreateValidatedToken(vo: TokenFO)
}

class TokenEntity(val accountsRepo: AccountsRepository, val clientsRepo: ClientsRepository) extends
  PersistentEntity[TokenState] with TokenFactory {

  import TokenEntity._

  override def initialState: TokenState = TokenInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CreateClientCredentialTokenCommand =>
      clientCredential.forward(o)
      state = CreatedValidationFO.validation

    case o: CreateValidatedClientCredentialTokenCommand =>
      clientCreator.forward(o)

    case CreateValidatedToken(vo) =>
      state = TokenPair(vo.id, vo.refreshId)
      persistAsync(TokenClientCredentialCreatedEvent(vo))(handleEventAndRespond())
  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case cmd: CreateClientCredentialTokenCommand => true
    case cmd: CreateAuthorizationCodeTokenCommand => true
    case cmd: CreatePasswordTokenCommand => true
    case cmd: CreateRefreshTokenCommand => true
    case _ => false
  }
  override def handleEvent(event: EntityEvent): Unit = event match {
    case TokenClientCredentialCreatedEvent(vo) =>
      state = vo
    case TokenAuthorizationCodeCreatedEvent(vo) =>
      state = vo
    case TokenPasswordCreatedEvent(vo) =>
      state = vo
    case TokenRefreshCreatedEvent(vo) =>
      state = vo
  }
}
