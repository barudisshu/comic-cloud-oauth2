package io.comiccloud.service.resources

import akka.actor.Props
import io.comiccloud.entity.PersistentEntity

object ResourceEntity {
  val Name           = "resource"
  def props(): Props = Props(new ResourceEntity())
}

class ResourceEntity() extends PersistentEntity[ResourceState] with ResourceFactory {

  override def initialState: ResourceState = ResourceInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CredentialsDeliverCommand =>
      state = ResourceFO.empty
      resourceCredential.forward(o)
  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case cmd: CredentialsDeliverCommand => true
    case _                              => false
  }
}
