package io.comiccloud.modeling.database

import com.outworkers.phantom.dsl._
import com.outworkers.util.testing._
import io.comiccloud.modeling.entity.Token
import io.comiccloud.utils.{CassandraSpec, TokenDbProvider, TokenGenerator}

import scala.concurrent.Future

class TokenDatabaseTest extends CassandraSpec with TokenGenerator with TokenDbProvider {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    database.create()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    database.truncate()
  }

  "A Token" should "be inserted into cassandra" in {
    val sample = gen[Token]
    val future = this.store(sample)

    whenReady(future) { result =>
      result isExhausted() shouldBe true
      result wasApplied() shouldBe true
      this.drop(sample)
    }
  }

  /**
   * Utility method to store into both tables
   *
   * @param token the token to be inserted
   * @return a [[Future]] of [[ResultSet]]
   */
  private def store(token: Token): Future[ResultSet] = TokenDatabase.saveOrUpdate(token)

  /**
   * Utility method to delete into both tables
   *
   * @param token the token to be deleted
   * @return a [[Future]] of [[ResultSet]]
   */
  private def drop(token: Token) = TokenDatabase.delete(token)

}
