package io.comiccloud.models

import java.sql.Timestamp

import io.comiccloud.config.Db

/**
  * The Resource Owner
  *
  * @param id        incrementing
  * @param uid       user identification
  * @param username  username, use for login
  * @param password  hashed with the salt - MD5 or SHA1
  * @param salt      unique for every user, inserted when the account is create
  * @param phone     optional. user mobile phone
  * @param email     required. while the user active for sending an email
  * @param createdAt while the user first invoke
  */
case class Account(id: Option[Int],
                   uid: String,
                   username: String,
                   password: String,
                   salt: String,
                   email: String,
                   phone: Option[String],
                   createdAt: Timestamp)

trait AccountsTable {
  this: Db =>

  import config.profile.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "ACCOUNTS") {
    // Columns
    def id = column[Int]("ID", O.PrimaryKey, O.Unique, O.AutoInc)
    def uid = column[String]("UID", O.Unique, O.Length(64), O.SqlType("VARCHAR"))
    def username = column[String]("USERNAME", O.Length(500))
    def password = column[String]("PASSWORD", O.Length(500))
    def salt = column[String]("SALT", O.Length(500), O.Unique)
    def email = column[String]("EMAIL", O.Length(100), O.SqlType("VARCHAR"))
    def phone = column[Option[String]]("PHONE", O.Length(20))
    def createdAt = column[Timestamp]("CREATED_AT", O.SqlType("timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

    // Indexes
    def usernameIndex = index("ACCOUNT_USERNAME_IDX", username, unique = true)
    def saltIndex = index("ACCOUNT_SALT_IDX", salt, unique = true)

    // Select
    override def * = (id.?, uid, username, password, salt, email, phone, createdAt) <> (Account.tupled, Account.unapply)
  }

  lazy val accounts: TableQuery[Accounts] = TableQuery[Accounts]
}

