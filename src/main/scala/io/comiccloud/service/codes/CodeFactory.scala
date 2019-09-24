package io.comiccloud.service.codes

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.service.codes.factor._

trait CodeFactory {
  this: Actor with ActorLogging =>

  val accountRepo: AccountsRepository
  val clientRepo: ClientsRepository

  def creator: ActorRef = context.actorOf(CodeCreator.props())
  def consumer: ActorRef = context.actorOf(CodeConsumer.props())
  def validator: ActorRef = context.actorOf(CodeCreateValidator.props(accountRepo, clientRepo))
  def findingByAccountId: ActorRef = context.actorOf(CodeFindingByAccountId.props(accountRepo))
  def findingByClientId: ActorRef = context.actorOf(CodeFindingByClientId.props(clientRepo))

}
