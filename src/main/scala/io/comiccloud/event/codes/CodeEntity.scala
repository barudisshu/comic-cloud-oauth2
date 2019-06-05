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

  case class CreateCode(ccc: CreateCodeCommand)
  case class CreateValidatedCode(ccc: CreateCodeCommand)
}

class CodeEntity(val clientsRepo: ClientsRepository,
                 val accountsRepo: AccountsRepository) extends PersistentEntity[CodeState] with CodeFactory {
  import CodeEntity._

  override def initialState: CodeState = CodeInitialState.empty

  override def additionalCommandHandling: Receive = {
    case o: CreateCodeCommand =>
      // before create, check out the accountUid and the clientUid is legal
    validator.forward(o)
      state = CodeReadyFO.validation
    case CreateValidatedCode(cmd) =>
      val state = cmd.vo
      persistAsync(CodeCreatedEvent(state))(handleEventAndRespond())

    // ========================================================================
    // atomicity operator show as below
    // ========================================================================

  }
  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case ccc: CreateCodeCommand => true
    case cvc: CreateValidatedCode => true
    case _ => false
  }
  override def handleEvent(event: EntityEvent): Unit = event match {
    case CodeCreatedEvent(codeFO) =>
      state = codeFO
  }
}
