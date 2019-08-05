package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Client

import scala.concurrent.Future

abstract class ClientByAccountModel extends Table[ClientByAccountModel, Client] {
  override def tableName: String = "client_by_account"

  object id extends TimeUUIDColumn with ClusteringOrder {
    override lazy val name = "id"
  }

  object owner_id     extends TimeUUIDColumn with PartitionKey
  object appid        extends StringColumn
  object appkey       extends StringColumn
  object redirect_uri extends OptionalStringColumn
  object grant_type   extends StringColumn
  object created_at   extends DateTimeColumn

  def getByAccount(owner_id: UUID): Future[List[Client]] = {
    select
      .where(_.owner_id eqs owner_id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .fetch()
  }

  def deleteByAccountAndId(owner_id: UUID, id: UUID): Future[ResultSet] = {
    delete
      .where(_.owner_id eqs owner_id)
      .and(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }
}
