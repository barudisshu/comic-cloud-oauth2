package io.comiccloud.service.codes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.datastax.driver.core.utils.UUIDs
import io.comiccloud.digest.Hashes
import io.comiccloud.rest.BasicRoutesDefinition
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.service.codes.CodeRouters.CreateCodeRequest
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

object CodeRouters {

  case class CreateCodeRequest(accountId: String, clientId: String, redirectUri: Option[String])

  object CreateCodeRequest {
    implicit val toJson: RootJsonFormat[CreateCodeRequest] = jsonFormat3(CreateCodeRequest.apply)
  }
}

class CodeRouters(codeRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {
  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    logRequest("server") {
      pathPrefix("code") {
        put {
          entity(as[CreateCodeRequest]) { request =>
            val vo = CodeFO(
              id = UUIDs.timeBased().toString,
              code = Hashes.randomHexString(5),
              accountId = request.accountId,
              appid = request.clientId,
              redirectUri = request.redirectUri
            )
            val command = CreateCodeCommand(vo)
            serviceAndComplete[CodeFO](command, codeRef)
          }
        }
      }
    }
  }
}
