package io.comiccloud.service.tokens

import io.comiccloud.entity.EntityFieldsObject
import io.comiccloud.rest.ServiceProtocol._
import org.joda.time.DateTime
import spray.json.RootJsonFormat

trait TokenState extends EntityFieldsObject[String, TokenState]

object TokenInitialState {
  def empty = TokenInitialState("")
}

case class TokenInitialState(id: String, deleted: Boolean = false) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def markDeleted: TokenState          = this
}

object CreatedValidationFO {
  def validation = CreatedValidationFO("")
}

case class CreatedValidationFO(id: String, deleted: Boolean = false) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def markDeleted: TokenState          = this
}

// four kind of mode
object TokenClientCredentialsFO {
  implicit val format: RootJsonFormat[TokenClientCredentialsFO] = jsonFormat3(TokenClientCredentialsFO.apply)
}
case class TokenClientCredentialsFO(id: String, appid: String, appkey: String) extends TokenState {
  override def deleted: Boolean = false
  override def assignId(id: String): TokenState = this.copy(id)
  override def markDeleted: TokenState = this
}

object TokenAuthorizationCodeFO {
  implicit val format: RootJsonFormat[TokenAuthorizationCodeFO] = jsonFormat5(TokenAuthorizationCodeFO.apply)
}
case class TokenAuthorizationCodeFO(id: String, appid: String, appkey: String, redirectUri: Option[String], code: String) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def deleted: Boolean = false
  override def markDeleted: TokenState = this
}

object TokenPasswordFO {
  implicit val format: RootJsonFormat[TokenPasswordFO] = jsonFormat5(TokenPasswordFO.apply)
}

case class TokenPasswordFO (id: String, appid: String, appkey: String, username: String, password: String) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def deleted: Boolean = false
  override def markDeleted: TokenState = this
}

object TokenRefreshTokenFO {
  implicit val format: RootJsonFormat[TokenRefreshTokenFO] = jsonFormat4(TokenRefreshTokenFO.apply)
}

case class TokenRefreshTokenFO(id: String, appid: String, appkey: String, refreshToken: String) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def deleted: Boolean = false
  override def markDeleted: TokenState = this
}

case class TokenDeleteFO(id: String, deleted: Boolean = true) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def markDeleted: TokenState = this
}

object TokenFO {
  def empty                                    = TokenFO("", "", "", "", "", "")
  implicit val format: RootJsonFormat[TokenFO] = jsonFormat9(TokenFO.apply)
}

case class TokenFO(id: String,
                   accountId: String,
                   appid: String,
                   appkey: String,
                   token: String,
                   refreshToken: String,
                   createdAt: DateTime = DateTime.now(),
                   expiredAt: DateTime = DateTime.now(),
                   deleted: Boolean = false)
    extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def markDeleted: TokenState          = this
}

object TokenPair {
  implicit val format: RootJsonFormat[TokenPair] = jsonFormat2(TokenPair.apply)
}

case class TokenPair(token: String, refreshToken: String) extends TokenState {
  override def assignId(id: String): TokenState = this.copy(id)
  override def id: String                       = token
  override def deleted: Boolean                 = false
  override def markDeleted: TokenState          = this
}
