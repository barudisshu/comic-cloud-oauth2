package io.comiccloud.service.tokens.response

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object TokenPO {
  implicit val format: RootJsonFormat[TokenPO] = jsonFormat3(TokenPO.apply)
}
case class TokenPO(accessToken: String, refreshToken: String, expiredAt: Long)
