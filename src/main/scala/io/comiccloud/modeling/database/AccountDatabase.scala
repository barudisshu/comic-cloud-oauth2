package io.comiccloud.modeling.database

import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.connector.Connector._
import io.comiccloud.modeling.entity.Account
import io.comiccloud.modeling.model._

import scala.concurrent.Future

class AccountDatabase(override val connector: CassandraConnection) extends Database[AccountDatabase](connector) {
  object AccountModel             extends AccountModel with connector.Connector
  object ClientModel              extends ClientModel with connector.Connector
  object CodeModel                extends CodeModel with connector.Connector
  object TokenModel  extends TokenModel with connector.Connector

  def saveOrUpdate(account: Account): Future[ResultSet] = {
    Batch.logged
      .add(AccountModel.store(account))
      .future()
  }

  def delete(account: Account): Future[ResultSet] = {
    Batch.logged
      .add(AccountModel.delete.where(_.id eqs account.id))
      .add(ClientModel.delete.where(_.owner_id eqs account.id))
      .add(CodeModel.delete.where(_.account_id eqs account.id))
      .add(TokenModel.delete.where(_.account_id eqs account.id))
      .future()
  }
}

object AccountDatabase extends AccountDatabase(connector)
