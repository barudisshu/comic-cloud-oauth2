package io.comiccloud.entity

import akka.actor._
import akka.cluster.sharding.ShardRegion
import io.comiccloud.rest._

import scala.language.postfixOps
import scala.reflect.ClassTag

object PersistentEntity {

  case object StopEntity

  case class GetState(id: String) extends EntityCommand {
    override def entityId: String = id
  }

  case class MarkAsDeleted(id: String) extends EntityCommand {
    override def entityId: String = id
  }

  class PersistentEntityIdExtractor(maxShards: Int) {

    val extractEntityId: ShardRegion.ExtractEntityId = {
      case ec: EntityCommand => (ec.entityId, ec)
    }

    val extractShardId: ShardRegion.ExtractShardId = {
      case ec: EntityCommand =>
        (math.abs(ec.entityId.hashCode) % maxShards).toString
    }
  }

  object PersistentEntityIdExtractor {

    def apply(system: ActorSystem): PersistentEntityIdExtractor = {
      val maxShards = system.settings.config.getInt("maxShards")
      new PersistentEntityIdExtractor(maxShards)
    }
  }
}

abstract class PersistentEntity[FO <: EntityFieldsObject[String, FO]: ClassTag]
    extends Actor
    with ActorLogging
    with EntityFactory {
  import PersistentEntity._
  import ShardRegion.Passivate

  import concurrent.duration._

  val id: String         = self.path.name
  val entityType: String = getClass.getSimpleName
  var state: FO          = initialState

  context.setReceiveTimeout(1 minute)

  def receive: Receive = standardCommandHandling orElse additionalCommandHandling

  def standardCommandHandling: Receive = {

    case ReceiveTimeout =>
      log.debug("{} entity with id {} is being passivated due to inactivity", entityType, id)
      context.parent ! Passivate(stopMessage = StopEntity)

    case StopEntity =>
      log.debug("{} entity with id {} is now being stopped due to inactivity", entityType, id)
      context stop self

    case cmd if !isAcceptingCommand(cmd) =>
      log.warning("Not allowing action {} on a deleted entity or an entity in the initial state with id {}", cmd, id)
      sender() ! stateResponse()

    case GetState(`id`) =>
      log.debug("get State by entity id {}", id)
      sender ! stateResponse()
  }

  def isAcceptingCommand(cmd: Any): Boolean =
    !state.deleted && !(state == initialState && !isCreateMessage(cmd))

  def additionalCommandHandling: Receive

  def isCreateMessage(cmd: Any): Boolean

  def initialState: FO

  def handleResponse(respectDeleted: Boolean = true)(): Unit = {
    sender() ! stateResponse(respectDeleted)
  }

  def stateResponse(respectDeleted: Boolean = true): ServiceResult[FO] =
    if (state == initialState) EmptyResult
    else if (respectDeleted && state.deleted) EmptyResult
    else FullResult(state)

}
