package io.comiccloud.modeling.model

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.entity.Token

import scala.concurrent.Future

abstract class TokenByRefreshTokenModel extends Table[TokenByRefreshTokenModel, Token] {
  override def tableName: String = "token_by_refresh"

  object account_id    extends TimeUUIDColumn with ClusteringOrder
  object appid         extends TimeUUIDColumn
  object access_token  extends StringColumn with ClusteringOrder
  object refresh_token extends StringColumn with PartitionKey
  object created_at    extends DateTimeColumn
  object expired_at extends DateTimeColumn

  def getByTokenId(refresh_token: String): Future[Option[Token]] = {
    select
      .where(_.refresh_token eqs refresh_token)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

}
