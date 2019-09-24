package io.comiccloud.service.clients

import akka.actor.{Actor, ActorLogging, Props}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.rest.FullResult
import io.comiccloud.service.clients.request.{CreateClientReq, FindClientByAccountIdReq, FindClientByIdReq}

object ClientActor {
  val Name = "client"

  def props(clientRepo: ClientsRepository, accountRepo: AccountsRepository): Props =
    Props(new ClientActor(clientRepo, accountRepo))

  case class CreateClient(ccc: CreateClientReq)
  case class CreateValidatedClient(ccv: CreateClientReq)

}
class ClientActor(val clientRepo: ClientsRepository, val accountRepo: AccountsRepository)
  extends Actor
    with ActorLogging
    with ClientFactory {

  import ClientActor._

  override def receive: Receive = {
    case o: CreateClientReq =>
      validator.forward(o)

    case CreateValidatedClient(cmd) =>
      sender() ! FullResult(cmd.vo)

    case cmd: FindClientByAccountIdReq =>
      finder.forward(cmd)

    case cmd: FindClientByIdReq =>
      finder.forward(cmd)

    case CreateClient(cmd) =>
      creator.forward(cmd)


  }
}