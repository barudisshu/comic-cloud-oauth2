package io.comiccloud.service.resources

import io.comiccloud.entity.EntityFieldsObject
import io.comiccloud.rest.ServiceProtocol._
import org.joda.time.DateTime
import spray.json.RootJsonFormat

import scala.concurrent.duration.FiniteDuration

trait ResourceState extends EntityFieldsObject[String, ResourceState]

object ResourceInitialState {
  def empty = ResourceInitialState("")
}

case class ResourceInitialState(id: String, deleted: Boolean = false) extends ResourceState {
  override def assignId(id: String): ResourceState = this.copy(id)
  override def markDeleted: ResourceState          = this
}

case class CredentialsFO(id: String, deleted: Boolean = false) extends ResourceState {
  override def assignId(id: String): ResourceState = this.copy(id)
  override def markDeleted: ResourceState          = this
}

object ResourceFO {
  implicit val format: RootJsonFormat[ResourceFO] = jsonFormat9(ResourceFO.apply)
}

case class ResourceFO(id: String,
                      accountId: String,
                      accountUsername: String,
                      accountEmail: String,
                      accountPhone: Option[String],
                      clientId: String,
                      redirectUri: Option[String] = None,
                      expiredAt: DateTime,
                      deleted: Boolean = false)
    extends ResourceState {
  override def assignId(id: String): ResourceState = this.copy(id)
  override def markDeleted: ResourceState          = this
}
