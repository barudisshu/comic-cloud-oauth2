package io.comiccloud.service.resources

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate
import io.comiccloud.event.resources.{CredentialsDeliverCommand, ResourceEntity, ResourceState}

object ResourceAssociate {
  val Name = "resource-associate"
  def props(): Props = Props(new ResourceAssociate())
}

class ResourceAssociate() extends Aggregate[ResourceState, ResourceEntity] {
  override def entityProps: Props = ResourceEntity.props()
  override def receive: Receive = {
    case command: CredentialsDeliverCommand =>
      forwardCommand(command)
  }
}
