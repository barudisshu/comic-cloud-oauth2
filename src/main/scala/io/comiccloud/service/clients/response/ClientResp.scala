package io.comiccloud.service.clients.response

import java.sql.Timestamp
import java.time.Instant

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object ClientResp {
  def empty                                       = ClientResp("", "", "", "", None, "")
  implicit val format: RootJsonFormat[ClientResp] = jsonFormat8(ClientResp.apply)
}

case class ClientResp(id: String,
                      ownerId: String,
                      clientId: String,
                      clientSecret: String,
                      redirectUri: Option[String],
                      grantType: String,
                      createdAt: Timestamp = Timestamp.from(Instant.now()),
                      expiredAt: Timestamp = Timestamp.from(Instant.now()))
