package io.comiccloud.entity

/**
 * Trait to mix into case classes that represent lightweight representations of the fields for
 * an entity modeled as an actor
 */
trait EntityFieldsObject[K, FO] extends Serializable {
  /**
   * Assigns an id to the fields object, returning a new instance
   * @param id The id to assign
   */
  def assignId(id: K): FO
  def id: K
  def deleted: Boolean
  def markDeleted: FO
}
