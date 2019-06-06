package io.comiccloud.event.tokens

import java.sql.Timestamp
import java.time.LocalDateTime

import io.comiccloud.entity.EntityFieldsObject
import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

trait TokenState extends EntityFieldsObject[String, TokenState]

object TokenInitialState {
  def empty = TokenInitialState("")
}

case class TokenInitialState(id: String, deleted: Boolean = false) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def markDeleted: TokenState = this
}

object CreatedValidationFO {
  def validation = CreatedValidationFO("")
}

case class CreatedValidationFO(id: String, deleted: Boolean = false) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def markDeleted: TokenState = this
}

object TokenFO {
  def empty = TokenFO("", None, "", "", "")
  implicit val format: RootJsonFormat[TokenFO] = jsonFormat7(TokenFO.apply)
}

case class TokenFO(id: String,
                   refreshId: Option[String],
                   appid: String,
                   appkey: String,
                   token: String,
                   createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now()),
                   deleted: Boolean = false) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def markDeleted: TokenState = this
}

object TokenPair {
  implicit val format: RootJsonFormat[TokenPair] = jsonFormat2(TokenPair.apply)
}

case class TokenPair(token: String, refreshToken: Option[String]) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def id: String = token
  override def deleted: Boolean = false
  override def markDeleted: TokenState = this
}
