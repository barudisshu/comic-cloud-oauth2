package io.comiccloud.repository

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import io.comiccloud.config.DbConfiguration
import io.comiccloud.models.Client
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.{FutureMatchers, OptionMatchers}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterEach

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

class ClientsRepositoryTest(implicit ee: ExecutionEnv) extends Specification
  with DbConfiguration
  with FutureMatchers
  with OptionMatchers
  with BeforeAfterEach {

  sequential
  val timeout: FiniteDuration = 500.milliseconds
  val clients = new ClientsRepository(config)

  def before: Unit = {
    Await.result(clients.init(), timeout)
  }

  def after: Unit = {
    Await.result(clients.drop(), timeout)
  }

  "Client be inserted successfully" >> {
    val currentTimestamp = Timestamp.valueOf(LocalDateTime.now())
    val client =
      Client(
        id = None,
        uid = UUID.randomUUID().toString,
        ownerId = "ownerId",
        clientId = "client_id",
        clientSecret = "client_secret",
        redirectUri = Some("redirect_uri"),
        createdAt = currentTimestamp
      )
    clients.insert(client) must be_==(client.copy(id = Some(1))).awaitFor(timeout)
  }
}
