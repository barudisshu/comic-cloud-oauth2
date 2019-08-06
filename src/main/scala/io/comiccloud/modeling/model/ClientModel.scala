package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Client

import scala.concurrent.Future

abstract class ClientModel extends Table[ClientModel, Client] {
  override def tableName: String = "client"

  object owner_id     extends TimeUUIDColumn with PartitionKey
  object appid        extends TimeUUIDColumn with PartitionKey
  object appkey       extends TimeUUIDColumn
  object redirect_uri extends OptionalStringColumn
  object grant_type   extends StringColumn
  object created_at   extends DateTimeColumn

  def getByClientId(id: UUID): Future[Option[Client]] = {
    select
      .where(_.appid eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

  def getByClientIdAndKey(id: UUID, key: UUID): Future[Option[Client]] = {
    getByClientId(id).map {
      case Some(c) => if (c.appkey == key) Some(c) else None
      case None    => None
    }
  }

  def deleteById(id: UUID): Future[ResultSet] = {
    delete
      .where(_.appid eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }
}
