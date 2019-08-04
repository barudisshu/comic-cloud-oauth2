package io.comiccloud.event.resources

import io.comiccloud.entity.EntityFieldsObject
import spray.json.RootJsonFormat
import io.comiccloud.rest.ServiceProtocol._

trait ResourceState extends EntityFieldsObject[String, ResourceState]

object ResourceInitialState {
  def empty = ResourceInitialState("")
}

case class ResourceInitialState(id: String, deleted: Boolean = false) extends ResourceState {
  override def assignId(id: String): ResourceState = this.copy(id)
  override def markDeleted: ResourceState = this
}

object ResourceFO {
  def empty = ResourceFO("", "")
  implicit val format: RootJsonFormat[ResourceFO] = jsonFormat3(ResourceFO.apply)
}

case class ResourceFO(id: String,
                      accountUid: String,
                      deleted: Boolean = false) extends ResourceState {
  override def assignId(id: String): ResourceState = this.copy(id)
  override def markDeleted: ResourceState = this
}