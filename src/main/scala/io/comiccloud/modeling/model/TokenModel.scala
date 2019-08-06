package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Token

import scala.concurrent.Future

abstract class TokenModel extends Table[TokenModel, Token] {
  override def tableName: String = "access_token"

  object id extends TimeUUIDColumn with PartitionKey
  object account_id extends TimeUUIDColumn with ClusteringOrder
  object appid extends TimeUUIDColumn with ClusteringOrder
  object access_token extends StringColumn
  object refresh_token extends OptionalStringColumn
  object created_at extends DateTimeColumn

  def getByTokenId(id: UUID): Future[Option[Token]] = {
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
