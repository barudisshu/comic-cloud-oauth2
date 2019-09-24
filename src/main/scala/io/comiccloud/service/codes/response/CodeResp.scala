package io.comiccloud.service.codes.response

import java.sql.Timestamp
import java.time.Instant
import java.util.Date

import spray.json.RootJsonFormat
import io.comiccloud.rest.ServiceProtocol._

object CodeResp {
  implicit val format: RootJsonFormat[CodeResp] = jsonFormat5(CodeResp.apply)
}

case class CodeResp(accountId: String,
                    clientId: String,
                    redirectUri: Option[String],
                    code: String,
                    createdAt: Date = Timestamp.from(Instant.now()))
