package io.comiccloud.models

import java.sql.Timestamp

import io.comiccloud.config.Db

/**
  * The Oauth2 Client
  *
  * @param id           incrementing
  * @param ownerId      user identification
  * @param grantType    grant type
  * @param clientId     client id
  * @param clientSecret client secret
  * @param redirectUri  redirect uri
  * @param createdAt    while the user authenticate for application
  */
case class Client(id: Option[Int],
                  ownerId: String,
                  grantType: String,
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
    def ownerId = column[String]("OWNER_ID", O.Unique, O.Length(64), O.SqlType("VARCHAR"))
    def grantType = column[String]("GRANT_TYPE", O.Length(20))
    def clientId = column[String]("CLIENT_ID", O.Length(100), O.Unique)
    def clientSecret = column[String]("CLIENT_SECRET", O.Length(100), O.Unique)
    def redirectUri = column[Option[String]]("REDIRECT_URI", O.Length(2000), O.SqlType("VARCHAR"))
    def createdAt = column[Timestamp]("CREATED_AT", O.SqlType("timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

    // Indexes
    def clientIdIndex = index("CLIENT_CLIENT_ID_IDX", clientId, unique = true)
    def clientSecretIndex = index("CLIENT_CLIENT_SECRET_IDX", clientSecret, unique = true)

    // Select
    override def * = (id.?, ownerId, grantType, clientId, clientSecret, redirectUri, createdAt) <> (Client.tupled, Client.unapply)
  }

  lazy val clients: TableQuery[Clients] = TableQuery[Clients]
}

