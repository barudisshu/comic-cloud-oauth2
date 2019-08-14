package io.comiccloud.service.codes

import akka.actor.Props
import io.comiccloud.aggregate.Aggregate

object CodeAssociate {
  val Name           = "code-associate"
  def props(): Props = Props(new CodeAssociate())
}

class CodeAssociate() extends Aggregate[CodeState, CodeEntity] {

  override def entityProps: Props = CodeEntity.props()
  override def receive: Receive = {
    case command: CreateCodeCommand =>
      forwardCommandWithoutSharding(command)
  }
}
