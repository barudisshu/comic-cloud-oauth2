package io.comiccloud.event.codes

import io.comiccloud.entity.EntityEvent

trait CodeEvent extends EntityEvent { def entityType: String = "code" }

case class CodeCreatedEvent(state: CodeState) extends CodeEvent

case class CodeFindEvent(state: CodeState) extends CodeEvent
