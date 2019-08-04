package io.comiccloud.event.resources.factor

import java.sql.Timestamp
import java.time.ZonedDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.event.resources.{CredentialsDeliverCommand, ResourceFO}
import io.comiccloud.models.Account

import scala.concurrent.Future

private[resources] object ResourceCredentialHandler {
  def props(): Props = Props(new ResourceCredentialHandler())
}

class ResourceCredentialHandler() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case o: CredentialsDeliverCommand =>
      context become verifiedToken(sender, o.entityId)
      // fetch the cassandra snapshot, loading account message and check client uri
      fetchSnapshot() pipeTo self
  }

  def verifiedToken(replyTo: ActorRef, token: String): Receive = {
    case Some(account: Account) =>
      val resourceFO = ResourceFO(
        id = token,
        accountUid = account.uid
      )
      replyTo ! Some(resourceFO)
      self ! PoisonPill

    case f: akka.actor.Status.Failure =>
      replyTo ! None
      context stop self
    case None =>
      replyTo ! None
      context stop self
  }

  def fetchSnapshot(): Future[Option[Account]] = {
    // todo: this is Fake, do you hup
    Future.successful {
      Option {
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

}
