package io.comiccloud.event.tokens

import io.comiccloud.entity.EntityEvent

trait TokenEvent extends EntityEvent { def entityType: String = "token" }

case class TokenClientCredentialCreatedEvent(token: TokenFO) extends TokenEvent
case class TokenAuthorizationCodeCreatedEvent(token: TokenFO) extends TokenEvent
case class TokenPasswordCreatedEvent(token: TokenFO) extends TokenEvent
case class TokenRefreshCreatedEvent(token: TokenFO) extends TokenEvent
