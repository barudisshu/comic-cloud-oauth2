package io.comiccloud.service.resources.factory

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import com.datastax.driver.core.utils.UUIDs
import io.comiccloud.modeling.entity.Account
import io.comiccloud.service.resources.{CredentialsDeliverCommand, ResourceFO}
import org.joda.time.DateTime

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
        accountUid = account.id.toString
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
          id = UUIDs.timeBased(),
          username = "username",
          password = "password",
          salt = "salt",
          email = "email",
          phone = None,
          created_at = DateTime.now()
        )
      }
    }
  }

}
