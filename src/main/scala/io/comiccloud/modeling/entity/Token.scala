package io.comiccloud.modeling.entity

import java.util.UUID

import org.joda.time.DateTime

case class Token(id: UUID,
                 account_id: UUID,
                 appid: UUID,
                 access_token: String,
                 refresh_token: Option[String],
                 created_at: DateTime)
