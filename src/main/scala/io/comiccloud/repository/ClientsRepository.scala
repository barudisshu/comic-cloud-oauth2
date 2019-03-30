package io.comiccloud.repository

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.comiccloud.config.Db
import io.comiccloud.models.{Client, ClientsTable}
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ClientsRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends Db with ClientsTable {

  import config.profile.api._

  def init(): Future[Unit] = db.run(DBIOAction.seq(clients.schema.create))

  def drop(): Future[Unit] = db.run(DBIOAction.seq(clients.schema.drop))

  def insert(client: Client): Future[Client] =
    db.run(clients returning clients.map(_.id) += client)
      .map(id => client.copy(id = Some(id)))

  def find(id: Int): Future[Option[Client]] =
    db.run((for (client <- clients if client.id === id) yield
      client).result.headOption)

  def findByClientId(clientId: String): Future[Option[Client]] =
    db.run((for (client <- clients if client.clientId ===
      clientId) yield client).result.headOption)

  def findAll(): Future[Seq[Client]] =
    db.run(clients.result)

  def update(id: Int, clientId: String, clientSecret: String): Future[Boolean] = {
    val query = for (client <- clients if client.id === id) yield (client.clientId, client.clientSecret)
    db.run(query.update(clientId, clientSecret)) map {
      _ > 0
    }
  }

  def delete(id: Int): Future[Boolean] =
    db.run(clients.filter(_.id === id).delete) map {
      _ > 0
    }

  def stream(implicit materializer: Materializer): Source[Client, NotUsed] =
    Source
      .fromPublisher(db.stream(clients.result.withStatementParameters(fetchSize = 10)))
}
