package io.comiccloud.event.auths

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.event.auths.factor.AuthVerify

trait AuthFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def verify: ActorRef = context.actorOf(AuthVerify.props())
}
