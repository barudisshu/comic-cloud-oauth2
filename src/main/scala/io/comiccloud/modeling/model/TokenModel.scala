package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Token

import scala.concurrent.Future

abstract class TokenModel extends Table[TokenModel, Token] {
  override def tableName: String = "access_token"

  object account_id extends TimeUUIDColumn with PartitionKey
  object appid extends TimeUUIDColumn with PartitionKey
  object access_token extends StringColumn with ClusteringOrder
  object refresh_token extends StringColumn with ClusteringOrder
  object created_at extends DateTimeColumn

  def getByAccessToken(access_token: String): Future[Option[Token]] = {
    select
      .where(_.access_token eqs access_token)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

}
