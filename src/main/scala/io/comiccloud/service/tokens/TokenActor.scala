package io.comiccloud.service.tokens

import akka.actor._
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest.FullResult
import io.comiccloud.service.tokens.TokenActor.CreateValidatedToken
import io.comiccloud.service.tokens.request.{CreateAuthorizationCodeTokenReq, CreateClientCredentialsTokenReq, CreatePasswordTokenReq, CreateRefreshTokenReq}
import io.comiccloud.service.tokens.response.{TokenPO, TokenResp}

import scala.language.postfixOps

object TokenActor {
  val Name = "token"
  def props(accountRepo: AccountsRepository,
            clientRepo: ClientsRepository,
            codeRef: ActorRef): Props =
    Props(new TokenActor(accountRepo, clientRepo, codeRef))

  case class CreateValidatedToken(vo: TokenResp)
}

/**
 * we don't need refresh token, the token within counter internal
 */
class TokenActor(val accountRepo: AccountsRepository,
                  val clientRepo: ClientsRepository,
                  val codeRef: ActorRef)
  extends Actor
    with ActorLogging
    with TokenFactory {

  override def receive: Receive = {
    case o: CreateClientCredentialsTokenReq =>
      clientCredentials.forward(o)

    case o: CreateAuthorizationCodeTokenReq =>
      authorizationCode.forward(o)

    case o: CreatePasswordTokenReq =>
      password.forward(o)

    case o: CreateRefreshTokenReq =>
      refreshToken.forward(o)

    case CreateValidatedToken(vo) =>
      sender() ! FullResult(TokenPO(vo.accessToken, vo.refreshToken, vo.expiredAt.getTime))
  }
}

