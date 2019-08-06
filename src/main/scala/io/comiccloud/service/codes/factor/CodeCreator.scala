package io.comiccloud.service.codes.factor

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import io.comiccloud.modeling.database.CodeDatabase
import io.comiccloud.modeling.entity.Code
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.codes.{CodeFO, CreateCodeCommand}

/**
  * the code will be persist in redis, for a while ...
  */
private[codes] object CodeCreator {
  def props(): Props = Props(new CodeCreator())
}

class CodeCreator() extends Actor with ActorLogging {
  import akka.pattern.pipe
  import context.dispatcher
  override def receive: Receive = {
    case CreateCodeCommand(vo) =>
      val code = Code(
        id = UUID.fromString(vo.id),
        account_id = UUID.fromString(vo.accountId),
        appid = UUID.fromString(vo.appid),
        code = vo.code,
        redirect_uri = vo.redirectUri,
        created_at = vo.createdAt
      )

    context become feedback(vo, sender)
      CodeDatabase.saveOrUpdate(code).map(rs => if(rs.isFullyFetched()) code else None) pipeTo self
  }

  def feedback(o: CodeFO, replyTo: ActorRef): Receive = {
    case c: Code =>
      replyTo ! FullResult(o)
      self ! PoisonPill

    case f: akka.actor.Status.Failure =>
      replyTo ! EmptyResult
      context stop self
    case None =>
      replyTo ! EmptyResult
      context stop self


  }
}