package io.comiccloud.service.tokens.response

import java.sql.Timestamp
import java.time.Instant
import java.util.Date

import io.comiccloud.models.Account
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.service.accounts.response.AccountResp
import io.comiccloud.service.clients.response.ClientResp
import spray.json.RootJsonFormat

object TokenResp {
  implicit val format: RootJsonFormat[TokenResp] = jsonFormat8(TokenResp.apply)
}

case class TokenResp(
                     accountId: String,
                     clientId: String,
                     accessToken: String,
                     refreshToken: String,
                     createdAt: Date = Timestamp.from(Instant.now()),
                     expiredAt: Date = Timestamp.from(Instant.now()),
                     account: AccountResp,
                     client: ClientResp)
