package io.comiccloud.repository

import io.comiccloud.config.Db
import io.comiccloud.models.{Client, ClientsTable}
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ClientsRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
    extends Db
    with ClientsTable {

  import config.profile.api._

  def init(): Future[Unit] = db.run(DBIOAction.seq(clients.schema.create))

  def drop(): Future[Unit] = db.run(DBIOAction.seq(clients.schema.drop))

  def insert(client: Client): Future[Client] = db.run(clients += client).map(_ => client)

  def find(uid: String): Future[Option[Client]] =
    db.run((for (client <- clients if client.uid === uid) yield client).result.headOption)

  def findByClientId(clientId: String): Future[Option[Client]] =
    db.run(
      (for (client <- clients if client.clientId ===
              clientId) yield client).result.headOption)

  def findByClientIdAndClientSecret(clientId: String, clientSecret: String): Future[Option[Client]] =
    db.run(
      (for (client <- clients if client.clientId === clientId && client.clientSecret === clientSecret)
        yield client).result.headOption
    )

  def findAll(): Future[Seq[Client]] =
    db.run(clients.result)

  def update(uid: String, clientId: String, clientSecret: String): Future[Boolean] = {
    val query = for (client <- clients if client.uid === uid) yield (client.clientId, client.clientSecret)
    db.run(query.update((clientId, clientSecret): (String, String))) map { _ > 0 }
  }

  def delete(uid: String): Future[Boolean] =
    db.run(clients.filter(_.uid === uid).delete) map { _ > 0 }
}
