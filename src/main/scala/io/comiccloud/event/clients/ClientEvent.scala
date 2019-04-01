package io.comiccloud.event.clients

import io.comiccloud.entity.EntityEvent

trait ClientEvent extends EntityEvent{ def entityType: String = "client" }

case class ClientCreatedEvent(client: ClientFO) extends ClientEvent
