package io.comiccloud.service.resources

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.Materializer
import io.comiccloud.event.accounts.AccountFO
import io.comiccloud.rest.BasicRoutesDefinition

import scala.concurrent.{ExecutionContext, Future}

object ResourceRouters {
  def oauth2Authenticator(resourceRef: ActorRef)(implicit ec: ExecutionContext): AsyncAuthenticator[AccountFO] = {
    case p@Credentials.Provided(identifier) =>
      Future {
        // potentially
        None
      }

    case _ => Future.successful(None)
  }
}

class ResourceRouters(resourceRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {

  import ResourceRouters._

  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    logRequestResult("server") {
      pathPrefix("resources") {
        get {
          authenticateOAuth2Async[AccountFO]("realm", oauth2Authenticator(resourceRef)) {
            auth => complete(OK, auth)
          }
        }
      }
    }
  }
}
