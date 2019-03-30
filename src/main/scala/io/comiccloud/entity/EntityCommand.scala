package io.comiccloud.entity

/**
 * Base trait for all entity based commands to extend from
 */
trait EntityCommand {

  /**
   * Gets the id of the entity that this command is for, to
   * use for shard routing
   * @return a String representing the entity id of this command
   */
  def entityId: String
}
