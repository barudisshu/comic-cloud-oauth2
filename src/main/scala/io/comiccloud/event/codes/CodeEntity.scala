package io.comiccloud.event.codes

import akka.actor.Props
import io.comiccloud.entity.{EntityEvent, PersistentEntity}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}

object CodeEntity {
  val Name = "code"
  def props(accountsRepo: AccountsRepository, clientsRepo: ClientsRepository): Props =
    Props(new CodeEntity(accountsRepo, clientsRepo))

  case class CreateCode(ccc: CreateCodeCommand)
  case class CreateValidatedCode(ccc: CreateCodeCommand)
  case class FindCodeById(cac: FindCodeByIdCommand)

}

class CodeEntity(val accountsRepo: AccountsRepository,
                 val clientsRepo: ClientsRepository) extends PersistentEntity[CodeState] with CodeFactory {

  import CodeEntity._

  override def initialState: CodeState = CodeInitialState.empty

  override def additionalCommandHandling: Receive = {

    case o: CreateCodeCommand =>
      validator.forward(o)
      state = CodeReadyFO.validation

    case CreateValidatedCode(cmd) =>
      val state = cmd.vo
      persistAsync(CodeCreatedEvent(state))(handleEventAndRespond())

    case o: FindCodeByIdCommand =>
      consumer.forward(o)

    case FindCodeById(cmd) =>
      persistAsync(CodeFindEvent(CodeTokenFO(cmd.entityId)))(handleEventAndRespond())

  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case ccc: CreateCodeCommand => true
    case cvc: CreateValidatedCode => true
    case _ => false
  }

  override def handleEvent(event: EntityEvent): Unit = event match {
    case CodeCreatedEvent(codeFO) =>
      state = codeFO
    case CodeFindEvent(codeFO) =>
      state = codeFO
  }
}
