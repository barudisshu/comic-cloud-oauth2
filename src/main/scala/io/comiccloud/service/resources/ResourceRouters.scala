package io.comiccloud.service.resources

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.BasicDirectives.provide
import akka.http.scaladsl.server.{Directive, Directive1, Route}
import akka.stream.Materializer
import io.comiccloud.rest.{ApiResponse, ApiResponseMeta, BasicRoutesDefinition, ErrorMessage}
import io.comiccloud.service.resources.request.CredentialsDeliverReq
import io.comiccloud.service.resources.response.{CredentialsResp, ResourceResp}

import scala.concurrent.ExecutionContext

class ResourceRouters(resourceRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {

  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    logRequestResult("server") {
      pathPrefix("resources") {
        (get & pathEnd) {
          authenticated {
            case Some(o2bt) =>
              val command = CredentialsDeliverReq(CredentialsResp(o2bt.token))
              serviceAndComplete[ResourceResp](command, resourceRef)
            case None =>
              val apiResp = ApiResponse[String](
                ApiResponseMeta(
                  Unauthorized.intValue,
                  Some(ErrorMessage("The resource requires authentication, which was not supplied with the request"))))
              complete((Unauthorized, apiResp))
          }
        }
      } ~ pathPrefix("benchmark") {
        (get & pathEnd) {
          complete("Hello World")
        }
      }
    }
  }

  private def authenticated: Directive1[Option[OAuth2BearerToken]] =
    extractCredentials.flatMap {
      case Some(c: OAuth2BearerToken) ⇒ provide(Some(c))
      case _ ⇒ extractAccessTokenParameterAsBearerToken
    }

  def extractAccessTokenParameterAsBearerToken: Directive[Tuple1[Option[OAuth2BearerToken]]] = {
    import akka.http.scaladsl.server.Directives._
    parameter('access_token.?).map(_.map(OAuth2BearerToken))
  }
}
