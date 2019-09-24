package io.comiccloud.repository

import io.comiccloud.config.Db
import io.comiccloud.models.{Account, AccountsTable}
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class AccountsRepository(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
    extends Db
    with AccountsTable {

  import config.profile.api._

  def init(): Future[Unit] = db.run(DBIOAction.seq(accounts.schema.create))
  def drop(): Future[Unit] = db.run(DBIOAction.seq(accounts.schema.drop))

  def insert(account: Account): Future[Account] = db.run(accounts += account).map(_ => account)

  def find(uid: String): Future[Option[Account]] =
    db.run((for (account <- accounts if account.uid === uid) yield account).result.headOption)

  def findByUid(uid: String): Future[Option[Account]] =
    db.run((for (account <- accounts if account.uid === uid) yield account).result.headOption)

  def findByEmail(email: String): Future[Option[Account]] =
    db.run(
      (for (account <- accounts if account.email ===
              email) yield account).result.headOption)

  def findByUsername(username: String): Future[Option[Account]] =
    db.run((for (account <- accounts if account.username === username) yield account).result.headOption)

  def findAll(): Future[Seq[Account]] = db.run(accounts.result)

  def update(uid: String, phone: Option[String], email: String): Future[Boolean] = {
    val query = for (account <- accounts if account.uid === uid) yield (account.phone, account.email)
    db.run(query.update((phone, email):(Option[String], String))) map { _ > 0 }
  }

  def delete(uid: String): Future[Boolean] =
    db.run(accounts.filter(_.uid === uid).delete) map { _ > 0 }
}
