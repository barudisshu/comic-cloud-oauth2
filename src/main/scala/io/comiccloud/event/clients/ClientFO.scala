package io.comiccloud.event.clients

import java.sql.Timestamp
import java.time.LocalDateTime

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object ClientFO {
  implicit val format: RootJsonFormat[ClientFO] = jsonFormat7(ClientFO.apply)
}

case class ClientFO(id: String,
                    ownerId: String,
                    clientId: String,
                    clientSecret: String,
                    redirectUri: Option[String],
                    createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now),
                    deleted: Boolean = false)