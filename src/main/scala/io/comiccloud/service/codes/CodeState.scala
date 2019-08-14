package io.comiccloud.service.codes

import io.comiccloud.entity.EntityFieldsObject
import io.comiccloud.rest.ServiceProtocol._
import org.joda.time.DateTime
import spray.json.RootJsonFormat

trait CodeState extends EntityFieldsObject[String, CodeState]

object CodeInitialState {
  def empty = CodeInitialState("")
}

case class CodeInitialState(id: String, deleted: Boolean = false) extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState          = this
}

object CodeFO {
  def empty                                   = CodeFO("", "", "", None, "")
  implicit val format: RootJsonFormat[CodeFO] = jsonFormat7(CodeFO.apply)
}

case class CodeFO(id: String,
                  accountId: String,
                  appid: String,
                  redirectUri: Option[String],
                  code: String,
                  createdAt: DateTime = DateTime.now(),
                  deleted: Boolean = false)
    extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState          = this
}

object CodeReadyFO {
  def validation = CodeReadyFO("")
}

case class CodeReadyFO(id: String, deleted: Boolean = false) extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState          = this
}

case class CodeDeleteFO(id: String, deleted: Boolean = true) extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState = this
}

object CodeTokenFO {
  implicit val format: RootJsonFormat[CodeTokenFO] = jsonFormat2(CodeTokenFO.apply)
}

case class CodeTokenFO(id: String, deleted: Boolean = false) extends CodeState {
  override def assignId(id: String): CodeState = this.copy(id)
  override def markDeleted: CodeState          = this
}
