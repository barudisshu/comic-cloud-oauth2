package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Code

import scala.concurrent.Future

abstract class CodeModel extends Table[CodeModel, Code] {
  override def tableName: String = "code"

  object account_id extends TimeUUIDColumn with ClusteringOrder
  object appid extends TimeUUIDColumn with ClusteringOrder
  object code extends StringColumn with PartitionKey
  object redirect_uri extends OptionalStringColumn
  object created_at extends DateTimeColumn

  def getById(id: String): Future[Option[Code]] = {
    select
      .where(_.code eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

  def deleteById(id: String): Future[ResultSet] = {
    delete
      .where(_.code eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

}
