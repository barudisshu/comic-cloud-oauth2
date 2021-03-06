package io.comiccloud.service.accounts

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.datastax.driver.core.utils.UUIDs
import io.comiccloud.rest.BasicRoutesDefinition
import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.service.accounts.AccountRouters.CreateAccountRequest
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

object AccountRouters {

  case class CreateAccountRequest(
                                   username: String,
                                   password: String,
                                   salt: String,
                                   email: String,
                                   phone: Option[String])

  object CreateAccountRequest {
    implicit val toJson: RootJsonFormat[CreateAccountRequest] = jsonFormat5(CreateAccountRequest.apply)
  }
}
class AccountRouters(accountRef: ActorRef)(implicit val ec: ExecutionContext) extends BasicRoutesDefinition {
  override def routes(implicit system: ActorSystem, ec: ExecutionContext, mater: Materializer): Route = {
    logRequestResult("server") {
      pathPrefix("account") {
        put {
          entity(as[CreateAccountRequest]) { request =>
            val id      = UUIDs.timeBased().toString
            val vo      = AccountFO(
              id,
              request.username,
              request.password,
              request.salt,
              request.email,
              request.phone)
            val command = CreateAccountCommand(vo)
            serviceAndComplete[AccountFO](command, accountRef)
          }
        }
      }
    }
  }
}
