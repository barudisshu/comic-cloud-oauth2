package io.comiccloud.event.resources

import io.comiccloud.entity.EntityEvent

trait ResourceEvent extends EntityEvent { def entityType: String = "resource" }

case class CredentialVerifiedEvent(resourceFO: ResourceFO) extends ResourceEvent
