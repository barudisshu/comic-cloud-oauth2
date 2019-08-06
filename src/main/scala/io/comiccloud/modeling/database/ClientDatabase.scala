package io.comiccloud.modeling.database

import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.connector.Connector._
import io.comiccloud.modeling.database.CodeDatabase.CodeModel
import io.comiccloud.modeling.database.TokenDatabase.TokenModel
import io.comiccloud.modeling.entity.Client
import io.comiccloud.modeling.model._

import scala.concurrent.Future

class ClientDatabase(override val connector: CassandraConnection) extends Database[ClientDatabase](connector) {

  object ClientModel extends ClientModel with connector.Connector

  def saveOrUpdate(client: Client): Future[ResultSet] = {
    Batch.logged
      .add(ClientModel.store(client))
      .future()
  }

  def delete(client: Client): Future[ResultSet] = {
    Batch.logged
      .add(ClientModel.delete.where(_.id eqs client.id))
      .add(CodeModel.delete.where(_.appid eqs client.id))
      .add(TokenModel.delete.where(_.appid eqs client.id))
      .future()
  }

}

object ClientDatabase extends ClientDatabase(connector)
