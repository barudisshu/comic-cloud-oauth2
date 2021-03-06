package io.comiccloud.service.accounts

import io.comiccloud.entity.EntityFieldsObject
import io.comiccloud.rest.ServiceProtocol._
import org.joda.time.DateTime
import spray.json.RootJsonFormat

trait AccountState extends EntityFieldsObject[String, AccountState]

object AccountInitialState {
  def empty = AccountInitialState("")
}

case class AccountInitialState(id: String, deleted: Boolean = false) extends AccountState {
  override def assignId(id: String): AccountState = this.copy(id)
  override def markDeleted: AccountState          = this
}

object ValidationFO {
  def validation = ValidationFO("")
}

case class ValidationFO(id: String, deleted: Boolean = false) extends AccountState {
  override def assignId(id: String): AccountState = this.copy(id)
  override def markDeleted: AccountState          = this
}

object AccountFO {
  def empty                                      = AccountFO("", "", "", "", "", None)
  implicit val format: RootJsonFormat[AccountFO] = jsonFormat8(AccountFO.apply)

}

case class AccountFO(id: String,
                     username: String,
                     password: String,
                     salt: String,
                     email: String,
                     phone: Option[String],
                     createdAt: DateTime = DateTime.now(),
                     deleted: Boolean = false)
    extends AccountState {
  override def assignId(id: String): AccountState = this.copy(id)
  override def markDeleted: AccountState          = this
}
