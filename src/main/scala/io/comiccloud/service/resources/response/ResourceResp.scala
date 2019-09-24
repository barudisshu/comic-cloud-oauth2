package io.comiccloud.service.resources.response

import io.comiccloud.rest.ServiceProtocol._
import io.comiccloud.service.tokens.response.TokenResp
import spray.json.RootJsonFormat

object ResourceResp {
  implicit val format: RootJsonFormat[ResourceResp] = jsonFormat6(ResourceResp.apply)
  def parall(tk: Any): Option[ResourceResp] = {
    tk match {
      case o: TokenResp =>
        Option {
          ResourceResp(
            accountId = o.accountId,
            accountUsername = o.account.username,
            accountEmail = o.account.email,
            accountPhone = o.account.phone,
            clientId = o.clientId,
            redirectUri = o.client.redirectUri
          )
        }
      case _ => None
    }
  }
}

case class ResourceResp(accountId: String,
                        accountUsername: String,
                        accountEmail: String,
                        accountPhone: Option[String],
                        clientId: String,
                        redirectUri: Option[String] = None)
