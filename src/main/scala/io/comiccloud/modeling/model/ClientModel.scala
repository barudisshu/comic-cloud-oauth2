package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Client

import scala.concurrent.Future

abstract class ClientModel extends Table[ClientModel, Client] {
  override def tableName: String = "client"

  object id extends TimeUUIDColumn with PartitionKey {
    override lazy val name = "id"
  }

  object owner_id     extends TimeUUIDColumn
  object appid        extends StringColumn
  object appkey       extends StringColumn
  object redirect_uri extends OptionalStringColumn
  object grant_type   extends StringColumn
  object created_at   extends DateTimeColumn

  def getByClientId(id: UUID): Future[Option[Client]] = {
    select
      .where(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

  def deleteById(id: UUID): Future[ResultSet] = {
    delete
      .where(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }
}
