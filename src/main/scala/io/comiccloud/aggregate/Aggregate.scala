package io.comiccloud.aggregate

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import io.comiccloud.entity._

import scala.reflect.ClassTag

abstract class Aggregate[FO <: EntityFieldsObject[String, FO], E <: PersistentEntity[FO] : ClassTag] extends Aggregator {

  val idExtractor = PersistentEntity.PersistentEntityIdExtractor(context.system)

  val entityShardingRegion: ActorRef =
    ClusterSharding(context.system).start(
      typeName = entityName,
      entityProps = entityProps,
      settings = ClusterShardingSettings(context.system),
      extractEntityId = idExtractor.extractEntityId,
      extractShardId = idExtractor.extractShardId
    )

  def entityProps: Props

  private def entityName = {
    val entityTag = implicitly[ClassTag[E]]
    entityTag.runtimeClass.getSimpleName
  }

  def forwardCommand(command: EntityCommand): Unit =
    entityShardingRegion.forward(command)
}
