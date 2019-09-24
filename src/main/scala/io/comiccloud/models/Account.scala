package io.comiccloud.models

import java.sql.Timestamp

import io.comiccloud.config.Db


/**
  * The Resource Owner
  *
  * @param uid       user identification
  * @param username  username, use for login
  * @param password  hashed with the salt - MD5 or SHA1
  * @param salt      unique for every user, inserted when the account is create
  * @param phone     optional. user mobile phone
  * @param email     required. while the user active for sending an email
  * @param createdAt while the user first invoke
  */
case class Account(uid: String,
                   username: String,
                   password: String,
                   salt: String,
                   email: String,
                   phone: Option[String],
                   createdAt: Timestamp)

trait AccountsTable {
  this: Db =>

  import config.profile.api._

  class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
    // Columns
    def uid = column[String]("uid", O.PrimaryKey, O.Unique, O.Length(64), O.SqlType("VARCHAR"))
    def username = column[String]("username", O.Length(500))
    def password = column[String]("password", O.Length(500))
    def salt = column[String]("salt", O.Length(500), O.Unique)
    def email = column[String]("email", O.Length(100), O.SqlType("VARCHAR"))
    def phone = column[Option[String]]("phone", O.Length(20))
    def createdAt = column[Timestamp]("created_at", O.SqlType("timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

    // Indexes
    def usernameIndex = index("account_username_idx", username, unique = true)
    def saltIndex = index("account_salt_idx", salt, unique = true)

    // Select
    override def * = (uid, username, password, salt, email, phone, createdAt) <> (Account.tupled, Account.unapply)
  }

  lazy val accounts: TableQuery[Accounts] = TableQuery[Accounts]
}