package io.comiccloud.modeling.model

import java.util.UUID

import com.datastax.driver.core.ConsistencyLevel
import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Account

import scala.concurrent.Future

abstract class AccountModel extends Table[AccountModel, Account] {
  override def tableName: String = "account"

  object id         extends TimeUUIDColumn with PartitionKey
  object username   extends StringColumn with PartitionKey
  object password   extends StringColumn
  object salt       extends StringColumn
  object email      extends StringColumn
  object phone      extends OptionalStringColumn
  object created_at extends DateTimeColumn

  def getByAccountId(id: UUID): Future[Option[Account]] = {
    select
      .where(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

  def getByAccountUsername(username: String): Future[List[Account]] = {
    select
      .where(_.username eqs username)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .fetch()
  }

  def deleteById(id: UUID): Future[ResultSet] = {
    delete.where(_.id eqs id).consistencyLevel_=(ConsistencyLevel.ONE).future()
  }
}
