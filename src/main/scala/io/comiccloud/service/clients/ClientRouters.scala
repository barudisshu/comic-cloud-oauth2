package io.comiccloud.service.clients

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.comiccloud.rest.BasicRoutesDefinition
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.service.clients.ClientRouters.CreateClientRequest
import io.comiccloud.service.clients.request.CreateClientReq
import io.comiccloud.service.clients.response.ClientResp
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

object ClientRouters {

  case class CreateClientRequest(accountId: String, grantType: String, redirectUri: Option[String])

  object CreateClientRequest {
    implicit val toJson: RootJsonFormat[CreateClientRequest] = jsonFormat3(CreateClientRequest.apply)
  }
}

class ClientRouters(clientRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {
  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    logRequest("server") {
      pathPrefix("client") {
        put {
          entity(as[CreateClientRequest]) { request =>
            val clientId = UUID.randomUUID().toString
            val vo = ClientResp(
              id = clientId,
              ownerId = request.accountId,
              clientId = clientId,
              clientSecret = clientId,
              grantType = request.grantType,
              redirectUri = request.redirectUri
            )
            val command = CreateClientReq(vo)
            serviceAndComplete[ClientResp](command, clientRef)
          }
        }
      }
    }
  }
}
