package io.comiccloud.event.resources.factor

import java.sql.Timestamp
import java.time.ZonedDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.resources.{CredentialVerifiedCommand, ResourceFO}
import io.comiccloud.models.Account
import io.comiccloud.rest.{EmptyResult, FullResult}

import scala.concurrent.Future

private[resources] object ResourceCredentialHandler {
  def props(): Props = Props(new ResourceCredentialHandler())
}

class ResourceCredentialHandler() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case o: CredentialVerifiedCommand =>
      context become verifiedToken(sender, o.entityId)
      // fetch the cassandra snapshot, loading account message and check client uri
      fetchSnapshot() pipeTo self
  }

  def verifiedToken(replyTo: ActorRef, verifyId: String): Receive = {
    case Some(account: Account) =>
      val resourceFO = ResourceFO(
        id = verifyId,
        accountUid = account.uid
      )
      replyTo ! FullResult(resourceFO)
      self ! PoisonPill

    case f: akka.actor.Status.Failure =>
      replyTo ! EmptyResult
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self
  }

  def fetchSnapshot(): Future[Account] = {
    // todo: this is Fake, do you hup
    Future.successful {
      Account(
        id = Some(1),
        uid = "uid",
        username = "username",
        password = "password",
        salt = "salt",
        email = "email",
        phone = None,
        createdAt = Timestamp.from(ZonedDateTime.now().toInstant)
      )
    }
  }

}
