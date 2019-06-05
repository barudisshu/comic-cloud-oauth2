package io.comiccloud.event.codes

import io.comiccloud.entity.EntityEvent

trait CodeEvent extends EntityEvent { def entityType: String = "code" }

case class CodeCreatedEvent(client: CodeFO) extends CodeEvent