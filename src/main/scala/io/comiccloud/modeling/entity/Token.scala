package io.comiccloud.modeling.entity

import java.util.UUID

import org.joda.time.DateTime

case class Token(account_id: UUID, appid: UUID, access_token: String, refresh_token: String, created_at: DateTime)
