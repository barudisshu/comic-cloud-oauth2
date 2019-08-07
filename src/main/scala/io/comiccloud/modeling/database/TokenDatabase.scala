package io.comiccloud.modeling.database

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.connector.Connector._
import io.comiccloud.modeling.entity.Token
import io.comiccloud.modeling.model.{TokenByAccessTokenModel, TokenByRefreshTokenModel}
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class TokenDatabase(override val connector: CassandraConnection) extends Database[TokenDatabase](connector) {
  object TokenByAccessTokenModel  extends TokenByAccessTokenModel with connector.Connector
  object TokenByRefreshTokenModel extends TokenByRefreshTokenModel with connector.Connector

  def saveOrUpdate(token: Token): Future[ResultSet] = {
    var ttl = 5 minutes
    val now = DateTime.now()
    if (token.expired_at.isAfter(now)) {
      val mills = token.expired_at.toInstant.getMillis - now.toInstant.getMillis
      if (ttl.toMillis < mills) ttl = mills milliseconds
    }
    Batch.logged
      .add(TokenByAccessTokenModel.store(token).ttl(ttl))
      .add(TokenByRefreshTokenModel.store(token).ttl(ttl.-(2 minutes)))
      .future()
  }

  def delete(token: Token): Future[ResultSet] = {
    Batch.logged
      .add(TokenByAccessTokenModel.delete.where(_.access_token eqs token.access_token))
      .add(TokenByRefreshTokenModel.delete.where(_.refresh_token eqs token.refresh_token))
      .future()
  }
}

object TokenDatabase extends TokenDatabase(connector)
