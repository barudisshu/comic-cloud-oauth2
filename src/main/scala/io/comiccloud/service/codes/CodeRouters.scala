package io.comiccloud.service.codes

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.comiccloud.event.codes.{CodeFO, CreateCodeCommand}
import io.comiccloud.rest.BasicRoutesDefinition
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.service.codes.CodeRouters.CreateCodeRequest
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

object CodeRouters {

  case class CreateCodeRequest(accountUid: String, clientUid: String, redirectUri: Option[String])

  object CreateCodeRequest {
    implicit val toJson: RootJsonFormat[CreateCodeRequest] = jsonFormat3(CreateCodeRequest.apply)
  }
}

class CodeRouters(codeRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {
  override def routes(implicit system: ActorSystem, ec: ExecutionContext,
                      mater: Materializer): Route = {
    logRequest("server") {
      (put & path("client")) {
        entity(as[CreateCodeRequest]) {request =>
          val id = UUID.randomUUID().toString
          val vo = CodeFO(
            id = id,
            accountUid = request.accountUid,
            clientUid = request.clientUid,
            code = id,
            redirectUri = request.redirectUri
          )
          val command = CreateCodeCommand(vo)
          serviceAndComplete[CodeFO](command, codeRef)
        }
      }
    }
  }
}
