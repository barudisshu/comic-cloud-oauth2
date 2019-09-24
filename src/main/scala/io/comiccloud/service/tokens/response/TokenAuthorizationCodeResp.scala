package io.comiccloud.service.tokens.response

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object TokenAuthorizationCodeResp {
  implicit val format: RootJsonFormat[TokenAuthorizationCodeResp] = jsonFormat4(TokenAuthorizationCodeResp.apply)
}
case class TokenAuthorizationCodeResp(clientId: String,
                                      clientSecret: String,
                                      redirectUri: Option[String],
                                      code: String)
