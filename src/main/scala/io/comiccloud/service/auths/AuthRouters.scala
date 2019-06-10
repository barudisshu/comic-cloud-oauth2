package io.comiccloud.service.auths

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import io.comiccloud.event.auths._
import io.comiccloud.rest.BasicRoutesDefinition

import scala.concurrent.ExecutionContext

object AuthRouters {
}

class AuthRouters(authRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition with Directives {

  override def routes(implicit system: ActorSystem,
                      ec: ExecutionContext,
                      mater: Materializer): Route = {
    logRequestResult("server") {
      (patch & path("oauth" / "auth")) {
        optionalHeaderValueByType(classOf[Authorization]) { o2bt =>
          o2bt.collect {
            case Authorization(OAuth2BearerToken(token)) => token
          } match {
            case Some(token) =>
              val vo = AuthFO(token)
              val command = VerificationAuthCommand(vo)
              serviceAndComplete[AuthInfo](command, authRef)
            case None =>
              reject(AuthorizationFailedRejection)
          }
        }
      }
    }
  }
}
