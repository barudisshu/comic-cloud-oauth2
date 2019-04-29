package io.comiccloud.event.codes.factor

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

private[codes] object CodeCreateValidator {
  def props(clientsRepo: ClientsRepository, accountsRepo: AccountsRepository): Props =
    Props(new CodeCreateValidator(clientsRepo, accountsRepo))
}

/**
  * checkout the generation, if pass, feedback CodeReadyFO
  */
class CodeCreateValidator(clientsRepo: ClientsRepository,
                          accountsRepo: AccountsRepository) extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = ???
}
