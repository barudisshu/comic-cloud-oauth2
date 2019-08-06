package io.comiccloud.service.codes

import akka.actor.Props
import io.comiccloud.entity.PersistentEntity

object CodeEntity {
  val Name           = "code"
  def props(): Props = Props(new CodeEntity())

  case class CreateCode(ccc: CreateCodeCommand)
  case class CreateValidatedCode(ccc: CreateCodeCommand)

}

class CodeEntity() extends PersistentEntity[CodeState] with CodeFactory {

  import CodeEntity._

  override def initialState: CodeState = CodeInitialState.empty

  override def additionalCommandHandling: Receive = {

    case o: CreateCodeCommand =>
      validator.forward(o)
      state = CodeReadyFO.validation

    case CreateValidatedCode(cmd) =>
      state = cmd.vo
      creator.forward(cmd)

  }

  override def isCreateMessage(cmd: Any): Boolean = cmd match {
    case ccc: CreateCodeCommand   => true
    case cvc: CreateValidatedCode => true
    case _                        => false
  }
}
