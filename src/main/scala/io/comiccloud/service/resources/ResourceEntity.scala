package io.comiccloud.service.resources

import akka.actor.Props
import io.comiccloud.entity.PersistentEntity
import io.comiccloud.modeling.entity.{Account, Client, Token}

object ResourceEntity {
  val Name           = "resource"
  def props(): Props = Props(new ResourceEntity())

  case class HandleResourceInfo(token: Token, account: Account, client: Client)
  case object HandleResourceTokenMissing
  case object HandleResourceAccountMissing
  case object HandleResourceClientMissing
}

class ResourceEntity() extends PersistentEntity[ResourceState] with ResourceFactory {

  import io.comiccloud.service.resources.ResourceEntity._

  override def initialState: ResourceState = ResourceInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CredentialsDeliverCommand =>
      state = o.vo
      resourceCredential.forward(o)

    case h @ HandleResourceTokenMissing =>
      resourceComposer.forward(h)

    case h @ HandleResourceAccountMissing =>
      resourceComposer.forward(h)

    case h @ HandleResourceClientMissing =>
      resourceComposer.forward(h)

    case h: HandleResourceInfo =>
      resourceComposer.forward(h)
  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case cmd: CredentialsDeliverCommand => true
    case _                              => false
  }
}
