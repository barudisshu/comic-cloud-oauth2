package io.comiccloud.event.codes

import akka.actor.Props
import io.comiccloud.entity.{EntityEvent, PersistentEntity}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

/**
  * it's a service to generate `code` for grant_type=authorization_code
  *
  * as it was generated and destroy in the moment, it will be persistent in the journal
  *
  *
  * Code Generator, not an adaptor, not a consumer, ... the storage should contain the follow
  *
  * <ul>
  *   <li>codeUid(Sharding Id)</li>
  *   <li>accountUid</li>
  *   <li>clientUid</li>
  *   <li>code</li>
  *   <li>redirectUri</li>
  * </ul>
  */
object CodeEntity {
  val Name = "code"
  def props(clientsRepo: ClientsRepository, accountsRepo: AccountsRepository): Props =
    Props(new CodeEntity(clientsRepo, accountsRepo))

}

class CodeEntity(val clientsRepo: ClientsRepository,
                 val accountsRepo: AccountsRepository) extends PersistentEntity[CodeState] with CodeFactory {
  import CodeEntity._

  override def additionalCommandHandling: Receive = {
    case o: CreateCodeCommand =>
      // before create, check out the accountUid and the clientUid is legal
    validator.forward(o)
      state = CodeReadyFO.validation
  }
  override def isCreateMessage(cmd: Any): Boolean = ???
  override def initialState: CodeState = ???
  override def handleEvent(event: EntityEvent): Unit = ???
}
