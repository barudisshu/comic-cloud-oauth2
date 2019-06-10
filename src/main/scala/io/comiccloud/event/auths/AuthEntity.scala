package io.comiccloud.event.auths

import akka.actor._
import io.comiccloud.rest.FullResult

import scala.language.postfixOps

object AuthEntity {
  val Name = "auth"
  def props(): Props = Props(new AuthEntity())


}
class AuthEntity() extends Actor with ActorLogging with AuthFactory {

  override def receive: Receive = {
    case o: VerificationAuthCommand =>
      verify.forward(o)

    case VerifiedAuthCommand(vo) =>
      val user = User(
        userId = "a86675a4-8695-4431-9531-7edd2aaa9c04",
        username = "galudisu",
        email = "galudisu@gmail.com",
        phone = None
      )
      val authInfo = AuthInfo(
        user,
        "1d991691bde2f5f4908d20a5df56dd9040e796ac",
        None
      )
      sender() ! FullResult(authInfo)
  }

}
