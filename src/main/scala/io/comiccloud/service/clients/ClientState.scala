package io.comiccloud.service.clients

import java.sql.Timestamp
import java.time.LocalDateTime

import io.comiccloud.entity.EntityFieldsObject
import io.comiccloud.rest.ServiceProtocol._
import org.joda.time.DateTime
import spray.json.RootJsonFormat

trait ClientState extends EntityFieldsObject[String, ClientState]

object ClientInitialState {
  def empty = ClientInitialState("")
}

case class ClientInitialState(id: String, deleted: Boolean = false) extends ClientState {
  override def assignId(id: String): ClientState = this.copy(id)
  override def markDeleted: ClientState          = this
}

object ClientFO {
  def empty                                     = ClientFO("", "", "", "", None, "")
  implicit val format: RootJsonFormat[ClientFO] = jsonFormat8(ClientFO.apply)
}

case class ClientFO(id: String,
                    ownerId: String,
                    appid: String,
                    appkey: String,
                    redirectUri: Option[String],
                    grantType: String,
                    createdAt: DateTime = DateTime.now(),
                    deleted: Boolean = false)
    extends ClientState {
  override def assignId(id: String): ClientState = this.copy(id)
  override def markDeleted: ClientState          = this
}

object ValidationFO {
  def validation = ValidationFO("")
}

case class ValidationFO(id: String, deleted: Boolean = false) extends ClientState {
  override def assignId(id: String): ClientState = this.copy(id)
  override def markDeleted: ClientState          = this
}

object CreateClientFO {
  implicit val format: RootJsonFormat[CreateClientFO] = jsonFormat5(CreateClientFO.apply)
}

case class CreateClientFO(id: String,
                          ownerId: String,
                          redirectUri: Option[String],
                          createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now),
                          deleted: Boolean = false)
    extends ClientState {
  override def assignId(id: String): ClientState = this.copy(id)
  override def markDeleted: ClientState          = this
}
