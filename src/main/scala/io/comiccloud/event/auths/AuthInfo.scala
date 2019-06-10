package io.comiccloud.event.auths

import io.comiccloud.rest.ServiceProtocol._
import spray.json.RootJsonFormat

object User {
  implicit val toJson: RootJsonFormat[User] = jsonFormat4(User.apply)
}

case class User(userId: String, username: String, email: String, phone: Option[String])

object AuthInfo {
  implicit val toJson: RootJsonFormat[AuthInfo] = jsonFormat3(AuthInfo.apply)
}

case class AuthInfo(user: User, clientId: String, redirectUri: Option[String])
