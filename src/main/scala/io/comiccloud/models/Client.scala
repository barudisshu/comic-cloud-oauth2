package io.comiccloud.models

import java.sql.Timestamp

import io.comiccloud.config.Db

/**
  * The Oauth2 Client
  *
  * @param uid           incrementing
  * @param ownerId      user identification
  * @param grantType    grant type
  * @param clientId     client id
  * @param clientSecret client secret
  * @param redirectUri  redirect uri
  * @param createdAt    while the user authenticate for application
  */
case class Client(uid: String,
                  ownerId: String,
                  grantType: String,
                  clientId: String,
                  clientSecret: String,
                  redirectUri: Option[String],
                  createdAt: Timestamp)

trait ClientsTable {
  this: Db =>

  import config.profile.api._

  class Clients(tag: Tag) extends Table[Client](tag, "clients") {
    // Columns
    def uid = column[String]("uid", O.PrimaryKey, O.Unique, O.Length(64), O.SqlType("VARCHAR"))
    def ownerId = column[String]("owner_id", O.Length(64), O.SqlType("VARCHAR"))
    def grantType = column[String]("grant_type", O.Length(20))
    def clientId = column[String]("client_id", O.Length(100), O.Unique)
    def clientSecret = column[String]("client_secret", O.Length(100), O.Unique)
    def redirectUri = column[Option[String]]("redirect_uri", O.Length(2000), O.SqlType("VARCHAR"))
    def createdAt = column[Timestamp]("created_at", O.SqlType("timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))

    // Indexes
    def clientIdIndex = index("client_client_id_idx", clientId, unique = true)
    def clientSecretIndex = index("client_client_secret_idx", clientSecret, unique = true)

    // Select
    override def * = (uid, ownerId, grantType, clientId, clientSecret, redirectUri, createdAt) <> (Client.tupled, Client.unapply)
  }

  lazy val clients: TableQuery[Clients] = TableQuery[Clients]
}