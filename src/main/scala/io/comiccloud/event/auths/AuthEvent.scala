package io.comiccloud.event.auths

import io.comiccloud.entity.EntityEvent

trait AuthEvent extends EntityEvent { def entityType: String = "auth" }

case class AuthVerifiedEvent(auth: AuthFO) extends AuthEvent
