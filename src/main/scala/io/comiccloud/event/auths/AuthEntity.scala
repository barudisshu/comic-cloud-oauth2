package io.comiccloud.event.auths

import akka.actor._
import io.comiccloud.entity._
import io.comiccloud.repository._

import scala.language.postfixOps

object AuthEntity {
  val Name = "auth"
  def props(): Props = Props(new AuthEntity())


}
class AuthEntity() extends PersistentEntity[AuthState] with AuthFactory {

  import AuthEntity._

  override def initialState: AuthState = AuthInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: VerificationAuthCommand =>
      verify.forward(o)
      state = AuthVerifyFO(o.entityId)

    case VerifiedAuthCommand(vo) =>
      persistAsync(AuthVerifiedEvent(vo))(handleEventAndRespond())
  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case VerificationAuthCommand => true
    case VerifiedAuthCommand => true
    case _ => false
  }

  override def handleEvent(event: EntityEvent): Unit = event match {
    case AuthVerifiedEvent(vo) =>
      state = vo
  }
}
