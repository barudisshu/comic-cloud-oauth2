package io.comiccloud.service.clients

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.datastax.driver.core.utils.UUIDs
import io.comiccloud.digest.Hashes
import io.comiccloud.rest.BasicRoutesDefinition
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.service.clients.ClientRouters.CreateClientRequest
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
            val id = UUIDs.timeBased().toString
            val vo = ClientFO(
              id = id,
              ownerId = request.accountId,
              clientId = Hashes.randomMd5().toString,
              clientSecret = Hashes.randomMd5().toString,
              grantType = request.grantType,
              redirectUri = request.redirectUri
            )
            val command = CreateClientCommand(vo)
            serviceAndComplete[ClientFO](command, clientRef)
          }
        }
      }
    }
  }
}
