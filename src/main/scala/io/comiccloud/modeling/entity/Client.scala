package io.comiccloud.modeling.entity

import java.util.UUID

import org.joda.time.DateTime

case class Client(owner_id: UUID,
                  appid: UUID,
                  appkey: UUID,
                  redirect_uri: Option[String] = None,
                  grant_type: String,
                  created_at: DateTime)
