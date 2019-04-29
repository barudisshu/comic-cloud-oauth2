package io.comiccloud.event.codes

import java.sql.Timestamp
import java.time.LocalDateTime

import io.comiccloud.entity.EntityFieldsObject
import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

trait CodeState extends EntityFieldsObject[String, CodeState]

object CodeInitialState {
  def empty = CodeInitialState("")
}

case class CodeInitialState(id: String, deleted: Boolean = false) extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState = this
}
object CodeFO {
  def empty = CodeFO("", "", "", "", None)
  implicit val format: RootJsonFormat[CodeFO] = jsonFormat7(CodeFO.apply)
}

case class CodeFO(id: String,
                  accountUid: String,
                  clientUid: String,
                  code: String,
                  redirectUri: Option[String],
                  createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now),
                  deleted: Boolean = false) extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState = this
}

object CodeReadyFO {
  def validation = CodeReadyFO("")
}

case class CodeReadyFO(id: String, deleted: Boolean = false) extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState = this
}

object CreateCodeFO {
  implicit val format: RootJsonFormat[CreateCodeFO] = jsonFormat5(CreateCodeFO.apply)
}

case class CreateCodeFO(id: String,
                          ownerId: String,
                          redirectUri: Option[String],
                          createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now),
                          deleted: Boolean = false) extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState = this
}
