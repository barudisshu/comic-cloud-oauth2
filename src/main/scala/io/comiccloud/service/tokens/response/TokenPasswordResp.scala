package io.comiccloud.service.tokens.response

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object TokenPasswordResp {
  implicit val format: RootJsonFormat[TokenPasswordResp] = jsonFormat4(TokenPasswordResp.apply)
}
case class TokenPasswordResp(clientId: String, clientSecret: String, username: String, password: String)
