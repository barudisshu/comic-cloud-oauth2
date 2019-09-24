package io.comiccloud.service.clients

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.service.clients.factory._

trait ClientFactory {
  this: Actor with ActorLogging =>

  val clientRepo : ClientsRepository
  val accountRepo: AccountsRepository

  def creator: ActorRef = context.actorOf(ClientCreator.props(clientRepo))
  def validator: ActorRef = context.actorOf(ClientCreateValidator.props(clientRepo, accountRepo))
  def finder: ActorRef = context.actorOf(ClientFinder.props(clientRepo, accountRepo))

}
