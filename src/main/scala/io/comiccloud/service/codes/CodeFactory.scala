package io.comiccloud.service.codes

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.entity.EntityFactory
import io.comiccloud.service.codes.factor._

trait CodeFactory extends EntityFactory {
  this: Actor with ActorLogging =>

  def creator: ActorRef            = context.actorOf(CodeCreator.props())
  def validator: ActorRef          = context.actorOf(CodeCreateValidator.props())
  def findingByAccountId: ActorRef = context.actorOf(CodeFindingByAccountId.props())
  def findingByClientId: ActorRef  = context.actorOf(CodeFindingByClientId.props())

}
