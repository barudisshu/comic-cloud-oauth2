package io.comiccloud.modeling.entity

import java.util.UUID

import org.joda.time.DateTime

case class Client (id: UUID,
                   owner_id: UUID,
                   appid: String,
                   appkey: String,
                   redirect_uri: Option[String] = None,
                   grant_type: String,
                   created_at: DateTime)