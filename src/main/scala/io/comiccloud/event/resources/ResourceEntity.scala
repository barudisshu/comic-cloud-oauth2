package io.comiccloud.event.resources

import akka.actor.Props
import io.comiccloud.entity.{EntityEvent, PersistentEntity}

object ResourceEntity {
  val Name = "resource"
  def props(): Props = Props(new ResourceEntity())
}

class ResourceEntity() extends PersistentEntity[ResourceState] with ResourceFactory {
  import ResourceEntity._

  override def additionalCommandHandling: Receive = {
    case o: CredentialVerifiedCommand =>
      resourceCredential.forward(o)
      state = ResourceFO.empty

  }
  override def isCreateMessage(cmd: Any): Boolean = {
    case _ => false
  }
  override def initialState: ResourceState = ResourceInitialState.empty
  override def handleEvent(event: EntityEvent): Unit = event match {
    case CredentialVerifiedEvent(vo) =>
      state = vo
  }
}
