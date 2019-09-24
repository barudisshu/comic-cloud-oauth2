package io.comiccloud.service.tokens.response

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object TokenClientCredentialsResp {
  implicit val format: RootJsonFormat[TokenClientCredentialsResp] = jsonFormat2(TokenClientCredentialsResp.apply)
}
case class TokenClientCredentialsResp(clientId: String, clientSecret: String)
