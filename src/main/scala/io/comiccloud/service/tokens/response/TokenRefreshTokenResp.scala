package io.comiccloud.service.tokens.response

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object TokenRefreshTokenResp {
  implicit val toJson: RootJsonFormat[TokenRefreshTokenResp] = jsonFormat3(TokenRefreshTokenResp.apply)
}
case class TokenRefreshTokenResp(clientId: String, clientSecret: String, refreshToken: String)
