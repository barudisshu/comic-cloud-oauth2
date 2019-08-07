package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Token

import scala.concurrent.Future

abstract class TokenByAccessTokenModel extends Table[TokenByAccessTokenModel, Token] {
  override def tableName: String = "token_by_access"

  object account_id extends TimeUUIDColumn with ClusteringOrder
  object appid extends TimeUUIDColumn
  object access_token extends StringColumn with PartitionKey
  object refresh_token extends StringColumn with ClusteringOrder
  object created_at extends DateTimeColumn
  object expired_at extends DateTimeColumn

  def getByAccessToken(access_token: String): Future[Option[Token]] = {
    select
      .where(_.access_token eqs access_token)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

}
