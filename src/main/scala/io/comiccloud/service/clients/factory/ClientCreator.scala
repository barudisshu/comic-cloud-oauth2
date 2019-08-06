package io.comiccloud.service.clients.factory

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.modeling.database.ClientDatabase
import io.comiccloud.modeling.entity.Client
import io.comiccloud.rest._
import io.comiccloud.service.clients.{ClientFO, CreateClientCommand}

private[clients] object ClientCreator {
  def props(): Props = Props(new ClientCreator())
}

class ClientCreator() extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case CreateClientCommand(vo) =>
      val client = Client(
        owner_id = UUID.fromString(vo.ownerId),
        appid = UUID.fromString(vo.appid),
        appkey = UUID.fromString(vo.appkey),
        redirect_uri = vo.redirectUri,
        grant_type = vo.grantType,
        created_at = vo.createdAt
      )
      context become feedback(vo, sender)
      ClientDatabase.saveOrUpdate(client).map(x => if(x.isFullyFetched()) client else None) pipeTo self
  }

  def feedback(o: ClientFO, replyTo: ActorRef): Receive = {
    case cli: Client =>
      replyTo ! FullResult(o)
      self ! PoisonPill
    case f: akka.actor.Status.Failure =>
      replyTo ! Failure(FailureType.Service, ErrorMessage("500", Some(s"db exception: ${f.cause.getLocalizedMessage}")))
      self ! PoisonPill
    case _ =>
      replyTo ! EmptyResult
      context stop self
  }
}