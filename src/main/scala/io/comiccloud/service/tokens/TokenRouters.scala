package io.comiccloud.service.tokens

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.rest.{ApiResponse, ApiResponseMeta, BasicRoutesDefinition, ErrorMessage}
import io.comiccloud.service.tokens.TokenRouters._
import io.comiccloud.service.tokens.request.{CreateAuthorizationCodeTokenReq, CreateClientCredentialsTokenReq, CreatePasswordTokenReq, CreateRefreshTokenReq}
import io.comiccloud.service.tokens.response.{TokenAuthorizationCodeResp, TokenClientCredentialsResp, TokenPO, TokenPasswordResp, TokenRefreshTokenResp}
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

object TokenRouters {
  object BasicRequest {
    implicit val toJson: RootJsonFormat[BasicRequest] = jsonFormat1(BasicRequest.apply)
  }
  case class BasicRequest(grantType: String)
  object ClientCredentialsRequest {
    implicit val toJson: RootJsonFormat[ClientCredentialsRequest] = jsonFormat3(ClientCredentialsRequest.apply)
  }
  case class ClientCredentialsRequest(clientId: String, clientSecret: String, grantType: String)
  object AuthorizationCodeRequest {
    implicit val toJson: RootJsonFormat[AuthorizationCodeRequest] = jsonFormat5(AuthorizationCodeRequest.apply)
  }
  case class AuthorizationCodeRequest(clientId: String,
                                      clientSecret: String,
                                      redirectUri: Option[String],
                                      code: String,
                                      grantType: String)
  object PasswordRequest {
    implicit val toJson: RootJsonFormat[PasswordRequest] = jsonFormat5(PasswordRequest.apply)
  }
  case class PasswordRequest(clientId: String,
                             clientSecret: String,
                             username: String,
                             password: String,
                             grantType: String)

  object RefreshRequest {
    implicit val toJson: RootJsonFormat[RefreshRequest] = jsonFormat4(RefreshRequest.apply)
  }
  case class RefreshRequest(clientId: String, clientSecret: String, refreshToken: String, grantType: String)
}
class TokenRouters(tokenRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {
  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    logRequestResult("server") {
      pathPrefix("token") {
        post {
          entity(as[BasicRequest]) {
            case BasicRequest("client_credentials") =>
              entity(as[ClientCredentialsRequest]) { request =>
                val vo      = TokenClientCredentialsResp(
                  clientId = request.clientId,
                  clientSecret = request.clientSecret
                )
                val command = CreateClientCredentialsTokenReq(vo)
                serviceAndComplete[TokenPO](command, tokenRef)
              }
            case BasicRequest("authorization_code") =>
              entity(as[AuthorizationCodeRequest]) { request =>
                val vo      = TokenAuthorizationCodeResp(
                  clientId = request.clientId,
                  clientSecret = request.clientSecret,
                  redirectUri = request.redirectUri,
                  code = request.code
                )
                val command = CreateAuthorizationCodeTokenReq(vo)
                serviceAndComplete[TokenPO](command, tokenRef)
              }
            case BasicRequest("password") =>
              entity(as[PasswordRequest]) { request =>
                val vo      = TokenPasswordResp(
                  clientId = request.clientId,
                  clientSecret = request.clientSecret,
                  username = request.username,
                  password = request.password
                )
                val command = CreatePasswordTokenReq(vo)
                serviceAndComplete[TokenPO](command, tokenRef)
              }

            case BasicRequest("refresh_token") =>
              entity(as[RefreshRequest]) { request =>
                val vo = TokenRefreshTokenResp(
                  clientId = request.clientId,
                  clientSecret = request.clientSecret,
                  refreshToken = request.refreshToken
                )
                val command = CreateRefreshTokenReq(vo)
                serviceAndComplete[TokenPO](command, tokenRef)
              }

            case BasicRequest(grantType) =>
              complete(
                Unauthorized,
                ApiResponse[String](
                  ApiResponseMeta(Unauthorized.intValue, Some(ErrorMessage(s"un-support grant type: $grantType")))))
          }
        }
      }
    }
  }
}
