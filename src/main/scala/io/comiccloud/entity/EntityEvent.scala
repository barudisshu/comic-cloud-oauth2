package io.comiccloud.entity

/**
 * Marker trait for something that is an event generated as the result of a command
 */
trait EntityEvent extends Serializable {
  /**
   * Gets the string identifier of the entity this event is for, for tagging purposes
   */
  def entityType: String
}
