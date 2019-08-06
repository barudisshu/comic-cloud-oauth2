package io.comiccloud.modeling.entity

import java.util.UUID

import org.joda.time.DateTime

case class Code(account_id: UUID, appid: UUID, code: String, redirect_uri: Option[String] = None, created_at: DateTime)
