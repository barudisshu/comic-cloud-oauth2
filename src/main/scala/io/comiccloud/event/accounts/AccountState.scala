package io.comiccloud.event.accounts

import java.sql.Timestamp
import java.time.LocalDateTime

import io.comiccloud.entity.EntityFieldsObject
import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

trait AccountState extends EntityFieldsObject[String, AccountState]

object AccountInitialState {
  def empty = AccountInitialState("")
}

case class AccountInitialState(id: String, deleted: Boolean = false) extends AccountState {
  override def assignId(id: String): AccountState = this.copy(id)
  override def markDeleted: AccountState = this
}

object ValidationFO {
  def validation = ValidationFO("")
}

case class ValidationFO(id: String, deleted: Boolean = false) extends AccountState {
  override def assignId(id: String): AccountState = this.copy(id)
  override def markDeleted: AccountState = this
}


object AccountFO {
  def empty = AccountFO("", "", "", "", "", None)
  implicit val format: RootJsonFormat[AccountFO] = jsonFormat8(AccountFO.apply)

}

case class AccountFO(id: String,
                     username: String,
                     password: String,
                     salt: String,
                     email: String,
                     phone: Option[String],
                     createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now()),
                     deleted: Boolean = false) extends AccountState {
  override def assignId(id: String): AccountState = this.copy(id)
  override def markDeleted: AccountState = this
}
