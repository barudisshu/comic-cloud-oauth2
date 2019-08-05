package io.comiccloud.modeling.entity

import java.util.UUID

import org.joda.time.DateTime

case class Account(id: UUID,
                   username: String,
                   password: String,
                   salt: String,
                   email: String,
                   phone: Option[String],
                   created_at: DateTime)
