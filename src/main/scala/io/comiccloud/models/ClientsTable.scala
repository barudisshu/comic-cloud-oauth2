package io.comiccloud.models

import java.sql.Timestamp

import io.comiccloud.config.Db

/**
  * The Oauth2 Client, the default grant type is `authorization_code`
  *
  * @param id           incrementing
  * @param uid          client identification
  * @param ownerId      user identification
  * @param clientId     client id
  * @param clientSecret client secret
  * @param redirectUri  redirect uri
  * @param createdAt    while the user authenticate for application
  */
case class Client(id: Option[Int],
                  uid: String,
                  ownerId: String,
                  clientId: String,
                  clientSecret: String,
                  redirectUri: Option[String],
                  createdAt: Timestamp)

trait ClientsTable {
  this: Db =>

  import config.profile.api._

  class Clients(tag: Tag) extends Table[Client](tag, "CLIENTS") {
    // Columns
    def id = column[Int]("ID", O.PrimaryKey, O.Unique, O.AutoInc)
    def uid = column[String]("UID", O.Unique, O.Length(64), O.SqlType("VARCHAR"))
    def ownerId = column[String]("OWNER_ID", O.Unique, O.Length(64), O.SqlType("VARCHAR"))
    def clientId = column[String]("CLIENT_ID", O.Length(100), O.Unique)
    def clientSecret = column[String]("CLIENT_SECRET", O.Length(100), O.Unique)
    def redirectUri = column[Option[String]]("REDIRECT_URI", O.Length(2000), O.SqlType("VARCHAR"))
    def createdAt = column[Timestamp]("CREATED_AT", O.SqlType("timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

    // Indexes
    def clientIdIndex = index("CLIENT_CLIENT_ID_IDX", clientId, unique = true)
    def clientSecretIndex = index("CLIENT_CLIENT_SECRET_IDX", clientSecret, unique = true)

    // Select
    override def * = (id.?, uid, ownerId, clientId, clientSecret, redirectUri, createdAt) <> (Client.tupled, Client.unapply)
  }

  lazy val clients: TableQuery[Clients] = TableQuery[Clients]
}

