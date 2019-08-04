package io.comiccloud.event.resources

import akka.actor.Props
import io.comiccloud.entity.{EntityEvent, PersistentEntity}

object ResourceEntity {
  val Name = "resource"
  def props(): Props = Props(new ResourceEntity())
}

class ResourceEntity() extends PersistentEntity[ResourceState] with ResourceFactory {
  import ResourceEntity._

  override def initialState: ResourceState = ResourceInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CredentialsDeliverCommand =>
      resourceCredential.forward(o)
      state = ResourceFO.empty
  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case cmd: CredentialsDeliverCommand => true
    case _ => false
  }
  override def handleEvent(event: EntityEvent): Unit = event match {
    case CredentialVerifiedEvent(vo) =>
      state = vo
  }
}
