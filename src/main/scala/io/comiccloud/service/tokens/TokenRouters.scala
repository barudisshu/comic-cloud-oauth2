package io.comiccloud.service.tokens

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.comiccloud.event.tokens._
import io.comiccloud.rest.BasicRoutesDefinition
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.service.tokens.TokenRouters.ClientCredentialRequest
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

object TokenRouters {

  // client_credentials
  case class ClientCredentialRequest(appid: String, appkey: String, grantType: String)
  // authorization_code
  case class AuthorizationCodeRequest(appid: String, appkey: String, redirectUri: Option[String], code: String, grantType: String)
  // password
  case class PasswordRequest(appid: String, appkey: String, username: String, password: String, grantType: String)
  // refresh
  case class RefreshRequest(appid: String, appkey: String, refreshToken: String, grantType: String)

  object ClientCredentialRequest {
    implicit val toJson: RootJsonFormat[ClientCredentialRequest] = jsonFormat3(ClientCredentialRequest.apply)
  }
}
class TokenRouters(tokenRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {
  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    logRequestResult("server") {
      pathPrefix("token") {
        post {
          entity(as[ClientCredentialRequest]) { request =>
            val vo      = TokenFO(request.appid)
            val command = CreateClientCredentialTokenCommand(vo)
            serviceAndComplete[TokenFO](command, tokenRef)
          }
        }
      }
    }
  }
}
