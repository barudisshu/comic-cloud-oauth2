package io.comiccloud.service.codes.factor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.modeling.database.ClientDatabase
import io.comiccloud.service.CommonBehaviorResolver
import io.comiccloud.service.codes.FindCodeRelateClientIdCommand

object CodeFindingByClientId {
  def props(): Props = Props(new CodeFindingByClientId())
}

class CodeFindingByClientId() extends Actor with ActorLogging with CommonBehaviorResolver {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case FindCodeRelateClientIdCommand(clientId) =>
      context become resolveFindingClientById(sender)
      ClientDatabase.ClientModel.getByClientId(UUID.fromString(clientId)) pipeTo self
  }
}
