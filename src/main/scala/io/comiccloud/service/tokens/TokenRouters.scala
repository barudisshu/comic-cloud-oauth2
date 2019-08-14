package io.comiccloud.service.tokens

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.datastax.driver.core.utils.UUIDs
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.rest.{ApiResponse, ApiResponseMeta, BasicRoutesDefinition, ErrorMessage}
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

object TokenRouters {

  case class BasicRequest(grantType: String)

  object BasicRequest {
    implicit val toJson: RootJsonFormat[BasicRequest] = jsonFormat1(BasicRequest.apply)
  }

  // client_credentials
  case class ClientCredentialsRequest(appid: String, appkey: String, grantType: String)
  // authorization_code
  case class AuthorizationCodeRequest(appid: String,
                                      appkey: String,
                                      redirectUri: Option[String],
                                      code: String,
                                      grantType: String)
  // password
  case class PasswordRequest(appid: String, appkey: String, username: String, password: String, grantType: String)
  // refresh
  case class RefreshRequest(appid: String, appkey: String, refreshToken: String, grantType: String)

  object ClientCredentialsRequest {
    implicit val toJson: RootJsonFormat[ClientCredentialsRequest] = jsonFormat3(ClientCredentialsRequest.apply)
  }
  object AuthorizationCodeRequest {
    implicit val toJson: RootJsonFormat[AuthorizationCodeRequest] = jsonFormat5(AuthorizationCodeRequest.apply)
  }
  object PasswordRequest {
    implicit val toJson: RootJsonFormat[PasswordRequest] = jsonFormat5(PasswordRequest.apply)
  }
  object RefreshRequest {
    implicit val toJson: RootJsonFormat[RefreshRequest] = jsonFormat4(RefreshRequest.apply)
  }
}
class TokenRouters(tokenRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {

  import io.comiccloud.service.tokens.TokenRouters._

  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    logRequestResult("server") {
      pathPrefix("token") {
        post {
          entity(as[BasicRequest]) {
            case BasicRequest("client_credentials") =>
              entity(as[ClientCredentialsRequest]) { request =>
                val vo = TokenClientCredentialsFO(
                  id = UUIDs.timeBased().toString,
                  appid = request.appid,
                  appkey = request.appkey
                )
                val command = CreateClientCredentialsTokenCommand(vo)
                serviceAndComplete[TokenPair](command, tokenRef)
              }
            case BasicRequest("authorization_code") =>
              entity(as[AuthorizationCodeRequest]) { request =>
                val vo = TokenAuthorizationCodeFO(
                  id = UUIDs.timeBased().toString,
                  appid = request.appid,
                  appkey = request.appkey,
                  redirectUri = request.redirectUri,
                  code = request.code
                )
                val command = CreateAuthorizationCodeTokenCommand(vo)
                serviceAndComplete[TokenPair](command, tokenRef)
              }
            case BasicRequest("password") =>
              entity(as[PasswordRequest]) { request =>
                val vo = TokenPasswordFO(
                  id = UUIDs.timeBased().toString,
                  appid = request.appid,
                  appkey = request.appkey,
                  username = request.username,
                  password = request.password
                )
                val command = CreatePasswordTokenCommand(vo)
                serviceAndComplete[TokenPair](command, tokenRef)
              }
            case BasicRequest("refresh_token") =>
              entity(as[RefreshRequest]) { request =>
                val vo = TokenRefreshTokenFO(
                  id = UUIDs.timeBased().toString,
                  appid = request.appid,
                  appkey = request.appkey,
                  refreshToken = request.refreshToken
                )
                val command = CreateRefreshTokenCommand(vo)
                serviceAndComplete[TokenPair](command, tokenRef)
              }

            case BasicRequest(grantType) =>
            complete(Unauthorized,
              ApiResponse[String](ApiResponseMeta(Unauthorized.intValue,
                Some(ErrorMessage(s"un-support grant type: $grantType")))))
          }
        }
      }
    }
  }
}
