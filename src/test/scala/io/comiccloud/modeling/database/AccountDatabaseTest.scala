package io.comiccloud.modeling.database

import com.outworkers.phantom.dsl._
import com.outworkers.util.testing._
import io.comiccloud.modeling.entity.Account
import io.comiccloud.utils.{AccountDbProvider, AccountGenerator, CassandraSpec}

import scala.concurrent.Future

class AccountDatabaseTest extends CassandraSpec with AccountGenerator with AccountDbProvider {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    database.create()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    database.truncate()
  }

  "A Account" should "be inserted into cassandra" in {
    val sample = gen[Account]
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
    * @param account the account to be inserted
    * @return a [[Future]] of [[ResultSet]]
    */
  private def store(account: Account): Future[ResultSet] = AccountDatabase.saveOrUpdate(account)

  /**
    * Utility method to delete into both tables
    *
    * @param account the account to be deleted
    * @return a [[Future]] of [[ResultSet]]
    */
  private def drop(account: Account) = AccountDatabase.delete(account)

}
