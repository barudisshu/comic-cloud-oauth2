package io.comiccloud.modeling.model

import com.datastax.driver.core.ConsistencyLevel
import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Account

import scala.concurrent.Future

abstract class AccountByUsernameModel extends Table[AccountByUsernameModel, Account] {
  override def tableName: String = "account_by_username"

  object id         extends TimeUUIDColumn with ClusteringOrder
  object username   extends StringColumn with PartitionKey
  object password   extends StringColumn
  object salt       extends StringColumn
  object email      extends StringColumn
  object phone      extends OptionalStringColumn
  object created_at extends DateTimeColumn

  def getByAccountUsername(username: String): Future[List[Account]] = {
    select
      .where(_.username eqs username)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .fetch()
  }
}
