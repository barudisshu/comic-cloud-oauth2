package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Code

import scala.concurrent.Future

abstract class CodeModel extends Table[CodeModel, Code] {
  override def tableName: String = "authorization_code"

  object id extends TimeUUIDColumn with PartitionKey
  object account_id extends TimeUUIDColumn with ClusteringOrder
  object appid extends TimeUUIDColumn with ClusteringOrder
  object code extends StringColumn
  object redirect_uri extends OptionalStringColumn
  object created_at extends DateTimeColumn

  def getById(id: UUID): Future[Option[Code]] = {
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
