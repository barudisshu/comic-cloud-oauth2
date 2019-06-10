package io.comiccloud.event.accounts

import java.sql.Timestamp
import java.time.LocalDateTime

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object AccountInfo {
  implicit val format: RootJsonFormat[AccountInfo] = jsonFormat8(AccountInfo.apply)
}

case class AccountInfo(id: String,
                       username: String,
                       password: String,
                       salt: String,
                       email: String,
                       phone: Option[String],
                       createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now()),
                       deleted: Boolean = false)
