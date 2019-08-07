package io.comiccloud.service.resources.factory

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import com.datastax.driver.core.utils.UUIDs
import io.comiccloud.service.resources.ResourceEntity._
import io.comiccloud.service.resources.ResourceFO

object ResourceComposer {
  def props(): Props = Props(new ResourceComposer())
}
class ResourceComposer() extends Actor with ActorLogging {

  override def receive: Receive = {
    case HandleResourceTokenMissing =>
      sender() ! None
      self ! PoisonPill
    case HandleResourceAccountMissing =>
      sender() ! None
      self ! PoisonPill
    case HandleResourceClientMissing =>
      sender() ! None
      self ! PoisonPill
    case HandleResourceInfo(token, account, client) =>
      val resourceFO = ResourceFO(
        id = UUIDs.timeBased().toString,
        accountId = token.account_id.toString,
        accountUsername = account.username,
        accountEmail = account.email,
        accountPhone = account.phone,
        clientId = token.appid.toString,
        redirectUri = client.redirect_uri,
        expiredAt = token.expired_at
      )
      sender() ! resourceFO
      self ! PoisonPill
  }
}
