package io.comiccloud.modeling.database

import com.outworkers.phantom.dsl._
import io.comiccloud.modeling.connector.Connector._
import io.comiccloud.modeling.entity.Code
import io.comiccloud.modeling.model.CodeModel

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class CodeDatabase(override val connector: CassandraConnection) extends Database[CodeDatabase](connector) {
  object CodeModel extends CodeModel with connector.Connector

  def saveOrUpdate(code: Code, ttl: FiniteDuration = 5 minutes): Future[ResultSet] = {
    Batch.logged
      .add(CodeModel.store(code).ttl(ttl))
      .future()
  }

  def delete(code: Code): Future[ResultSet] = {
    Batch.logged
      .add(CodeModel.delete.where(_.code eqs code.code))
      .future()
  }
}

object CodeDatabase extends CodeDatabase(connector)
