package io.comiccloud.entity

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.persistence._
import io.comiccloud.rest._

import scala.language.postfixOps
import scala.reflect.ClassTag

object PersistentEntity {

  case object StopEntity

  case class GetState(id: String) extends EntityCommand{
    override def entityId: String = id
  }

  case class MarkAsDeleted(id: String) extends EntityCommand {
    override def entityId:String = id
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
  extends PersistentActor with ActorLogging {
  import PersistentEntity._
  import ShardRegion.Passivate
  import concurrent.duration._

  val id        : String      = self.path.name
  val entityType: String      = getClass.getSimpleName
  var state     : FO          = initialState
  var eventsSinceLastSnapshot = 0

  context.setReceiveTimeout(1 minute)

  override def persistenceId = s"$entityType-$id"

  def receiveRecover: Receive = standardRecover orElse customRecover

  def standardRecover: Receive = {

    case ev: EntityEvent =>
      log.info("Recovering persisted event: {}", ev)
      handleEvent(ev)
      eventsSinceLastSnapshot += 1

    case SnapshotOffer(meta, snapshot: FO) =>
      log.debug("Recovering entity with a snapshot: {} with meta: {}", snapshot, meta)
      state = snapshot

    case RecoveryCompleted =>
      log.debug("Recovery completed for {} entity with id {}", entityType, id)
  }

  def customRecover: Receive = PartialFunction.empty

  def receiveCommand: Receive = standardCommandHandling orElse additionalCommandHandling

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

    case MarkAsDeleted =>
      newDeleteEvent match {
        case None =>
          log.debug("The entity type {} does not support deletion, ignoring delete request", entityType)
          sender ! stateResponse()

        case Some(event) =>
          persist(event)(handleEventAndRespond(respectDeleted = false))
      }

    case _: SaveSnapshotSuccess =>
      log.debug("Successfully saved a new snapshot for entity {} and id {}", entityType, id)

    case f: SaveSnapshotFailure =>
      log.error(f.cause, "Failed to save a snapshot for entity {} and id {}, reason was {}", entityType)
  }

  def isAcceptingCommand(cmd: Any): Boolean =
    !state.deleted &&
      !(state == initialState && !isCreateMessage(cmd))

  def additionalCommandHandling: Receive

  def newDeleteEvent: Option[EntityEvent] = None

  def isCreateMessage(cmd: Any): Boolean

  def initialState: FO

  def stateResponse(respectDeleted: Boolean = true): ServiceResult[FO] =
    if (state == initialState) EmptyResult

    else if (respectDeleted && state.deleted) EmptyResult

    else FullResult(state)

  def handleEvent(event: EntityEvent): Unit

  def handleEventAndRespond(respectDeleted: Boolean = true)(event: EntityEvent): Unit = {
    handleEvent(event)
    if (snapshotAfterCount.isDefined) {
      eventsSinceLastSnapshot += 1
      maybeSnapshot()
    }
    sender() ! stateResponse(respectDeleted)
  }

  def snapshotAfterCount: Option[Int] = None

  def maybeSnapshot(): Unit = {
    snapshotAfterCount.
      filter(i => eventsSinceLastSnapshot >= i).
      foreach { i =>
        log.debug("Taking snapshot because event count {} is > snapshot event limit of {}", eventsSinceLastSnapshot, i)
        saveSnapshot(state)
        eventsSinceLastSnapshot = 0
      }
  }
}
