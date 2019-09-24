package io.comiccloud.service.codes

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import io.comiccloud.repository.{AccountsRepository, ClientsRepository}
import io.comiccloud.service.codes.request.{CreateCodeReq, FindCodeByIdReq}

object CodeAssociate {
  val Name = "code-associate"
  def props(accountRepo: AccountsRepository, clientRepo: ClientsRepository): Props =
    Props(new CodeAssociate(accountRepo, clientRepo))
}
class CodeAssociate(accountRepo: AccountsRepository, clientRepo: ClientsRepository)
    extends Actor
    with ActorLogging {

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case ec: CreateCodeReq   => (ec.vo.code, ec)
    case ec: FindCodeByIdReq => (ec.id, ec)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case ec: CreateCodeReq   => (math.abs(ec.vo.code.hashCode) % 100).toString
    case ec: FindCodeByIdReq => (math.abs(ec.id.hashCode)    % 100).toString
  }

  val entityShardingRegion: ActorRef = ClusterSharding(context.system).start(
    typeName = CodeActor.Name,
    entityProps = CodeActor.props(accountRepo, clientRepo),
    settings = ClusterShardingSettings(context.system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )

  override def receive: Receive = {
    case cmd: CreateCodeReq =>
      entityShardingRegion.forward(cmd)

    case cmd: FindCodeByIdReq =>
      entityShardingRegion.forward(cmd)

    case it =>
      log.error(s"$it")
    entityShardingRegion.forward(it)
  }
}
