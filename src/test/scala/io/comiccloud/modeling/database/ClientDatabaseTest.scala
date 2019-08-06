package io.comiccloud.modeling.database

import com.outworkers.phantom.dsl._
import com.outworkers.util.testing._
import io.comiccloud.modeling.entity.Client
import io.comiccloud.utils.{CassandraSpec, ClientDbProvider, ClientGenerator}

import scala.concurrent.Future

class ClientDatabaseTest extends CassandraSpec with ClientGenerator with ClientDbProvider {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    database.create()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    database.truncate()
  }

  "A Client" should "be inserted into cassandra" in {
    val sample = gen[Client]
    val future = this.store(sample)

    whenReady(future) { result =>
      result isExhausted () shouldBe true
      result wasApplied () shouldBe true
      this.drop(sample)
    }
  }

  /**
    * Utility method to store into both tables
    *
    * @param client the client to be inserted
    * @return a [[Future]] of [[ResultSet]]
    */
  private def store(client: Client): Future[ResultSet] = ClientDatabase.saveOrUpdate(client)

  /**
    * Utility method to delete into both tables
    *
    * @param client the client to be deleted
    * @return a [[Future]] of [[ResultSet]]
    */
  private def drop(client: Client) = ClientDatabase.delete(client)

}
