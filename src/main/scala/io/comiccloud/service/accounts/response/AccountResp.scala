package io.comiccloud.service.accounts.response

import java.sql.Timestamp
import java.time.Instant

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object AccountResp {
  implicit val format: RootJsonFormat[AccountResp] = jsonFormat7(AccountResp.apply)

}

case class AccountResp(id: String,
                       username: String,
                       password: String,
                       salt: String,
                       email: String,
                       phone: Option[String],
                       createdAt: Timestamp = Timestamp.from(Instant.now()))
