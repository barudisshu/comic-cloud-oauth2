package io.comiccloud.repository

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import io.comiccloud.config.Db
import io.comiccloud.models.{Account, AccountsTable}
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class AccountsRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends Db with AccountsTable {

  import config.profile.api._

  def init(): Future[Unit] = db.run(DBIOAction.seq(accounts.schema.create))
  def drop(): Future[Unit] = db.run(DBIOAction.seq(accounts.schema.drop))

  def insert(account: Account): Future[Account] = db
                                                  .run(accounts returning accounts.map(_.id) += account)
                                                  .map(id => account.copy(id = Some(id)))

  def find(id: Int): Future[Option[Account]] = db.run((for (account <- accounts if account.id === id) yield
    account).result.headOption)
  def findByUid(uid: String): Future[Option[Account]] = db.run((for (account <- accounts if account.uid === uid) yield account).result.headOption)
  def findByEmail(email: String): Future[Option[Account]] = db.run((for (account <- accounts if account.email ===
    email) yield account).result.headOption)
  def findAll(): Future[Seq[Account]] = db.run(accounts.result)

  def update(id: Int, phone: Option[String], email: String): Future[Boolean] = {
    val query = for (account <- accounts if account.id === id) yield (account.phone, account.email)
    db.run(query.update(phone, email)) map { _ > 0 }
  }

  def delete(id: Int): Future[Boolean] =
    db.run(accounts.filter(_.id === id).delete) map { _ > 0 }

  def stream(implicit materializer: Materializer): Source[Account, NotUsed] =
    Source
    .fromPublisher(db.stream(accounts.result.withStatementParameters(fetchSize = 10)))
}
