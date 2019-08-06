package io.comiccloud.modeling.database

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.connector.Connector._
import io.comiccloud.modeling.entity.Token
import io.comiccloud.modeling.model.TokenModel

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class TokenDatabase(override val connector: CassandraConnection) extends Database[TokenDatabase](connector) {
  object TokenModel extends TokenModel with connector.Connector

  def saveOrUpdate(token: Token, ttl: FiniteDuration = 1 days): Future[ResultSet] = {
    Batch.logged
      .add(TokenModel.store(token).ttl(ttl))
      .future()
  }

  def delete(token: Token): Future[ResultSet] = {
    Batch.logged
      .add(TokenModel.delete.where(_.id eqs token.id))
      .future()
  }
}

object TokenDatabase extends TokenDatabase(connector)
