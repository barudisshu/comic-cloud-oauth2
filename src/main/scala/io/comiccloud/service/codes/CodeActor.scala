package io.comiccloud.service.codes

import akka.actor.{Actor, ActorLogging, Props, ReceiveTimeout}
import akka.cluster.sharding.ShardRegion.Passivate
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest.{EmptyResult, FullResult}
import io.comiccloud.service.codes.request.{CreateCodeReq, FindCodeByIdReq}
import io.comiccloud.service.codes.response.CodeResp

object CodeActor {
  val Name = "code"
  def props(accountRepo: AccountsRepository, clientRepo: ClientsRepository): Props =
    Props(new CodeActor(accountRepo, clientRepo))

  case class CreateCode(ccc: CreateCodeReq)
  case class CreateValidatedCode(ccc: CreateCodeReq)
  case object FindCodeById

  case object StopEntity

}
class CodeActor(val accountRepo: AccountsRepository, val clientRepo: ClientsRepository)
    extends Actor
    with ActorLogging
    with CodeFactory {

  import CodeActor._

  val id: String         = self.path.name
  val entityType: String = getClass.getSimpleName

  var state: Option[CodeResp] = None

  override def receive: Receive = {
    case o: CreateCodeReq =>
      validator.forward(o)

    case CreateValidatedCode(cmd) =>
      state = Some(cmd.vo)
      handleRespond()

    case o: FindCodeByIdReq =>
      consumer.forward(o)

    case FindCodeById =>
      handleRespond()

    case ReceiveTimeout =>
      context.parent ! Passivate(stopMessage = StopEntity)

    case StopEntity =>
      context stop self
  }

  private def handleRespond(): Unit = {
    state match {
      case Some(codeResp) => sender ! FullResult(codeResp)
      case None => sender ! EmptyResult
    }
  }
}
