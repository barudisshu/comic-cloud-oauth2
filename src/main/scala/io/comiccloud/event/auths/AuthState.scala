package io.comiccloud.event.auths

import io.comiccloud.entity.EntityFieldsObject

trait AuthState extends EntityFieldsObject[String, AuthState]

object AuthInitialState {
  def empty = AuthInitialState("")
}

case class AuthInitialState(id: String, deleted: Boolean = false) extends AuthState {
  override def assignId(id: String): AuthState = this.copy(id)
  override def markDeleted: AuthState = this
}
case class AuthVerifyFO(id: String, deleted: Boolean = false) extends AuthState {
  override def assignId(id: String): AuthState = this.copy(id)
  override def markDeleted: AuthState = this
}

case class AuthFO(id: String, deleted: Boolean = false) extends AuthState {
  override def assignId(id: String): AuthState = this.copy(id)
  override def markDeleted: AuthState = this
}
