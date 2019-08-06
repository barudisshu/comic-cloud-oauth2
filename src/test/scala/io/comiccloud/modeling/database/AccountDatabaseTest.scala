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

  "A Account" should "be deleted by id" in {
    val sample = gen[Account]

    val chain = for {
      _      <- this.store(sample)
      get    <- database.AccountModel.getByAccountId(sample.id)
      delete <- database.AccountModel.deleteById(sample.id)
    } yield (get, delete)

    whenReady(chain) {
      case (res, deleted) =>
        res shouldBe defined
        res.get shouldEqual sample

        deleted isExhausted () shouldBe true
        deleted wasApplied () shouldBe true
        this.drop(sample)
    }
  }

  it should "find an account by id" in {
    val sample = gen[Account]

    val chain = for {
      _   <- this.store(sample)
      get <- database.AccountModel.getByAccountId(sample.id)
      _   <- this.drop(sample)
    } yield get

    whenReady(chain) { res =>
      res shouldBe defined
      res.get shouldEqual sample
      this.drop(sample)
    }
  }

  it should "find accounts by username" in {
    val sample  = gen[Account]
    val sample2 = gen[Account]
    val sample3 = gen[Account]

    val accountByUsername = (for {
      f1 <- this.store(sample.copy(password = "123"))
      f2 <- this.store(sample2.copy(password = "456"))
      f3 <- this.store(sample3.copy(password = "789"))
    } yield (f1, f2, f3)).flatMap { _ =>
      database.AccountByUsernameModel.getByAccountUsername("Galudisu")
    }

    whenReady(accountByUsername) { searchResult =>
      searchResult shouldBe a[List[_]]
      searchResult should have length 3
      this.drop(sample)
      this.drop(sample2)
      this.drop(sample3)
    }
  }

  it should "be updated into cassandra" in {
    val sample       = gen[Account]
    val updatedTitle = gen[String]

    val chain = for {
      _          <- this.store(sample)
      unmodified <- database.AccountModel.getByAccountId(sample.id)
      _          <- this.store(sample.copy(username = updatedTitle))
      modified   <- database.AccountModel.getByAccountId(sample.id)
    } yield (unmodified, modified)

    whenReady(chain) {
      case (initial, modified) =>
        initial shouldBe defined
        initial.value.username shouldEqual sample.username

        modified shouldBe defined
        modified.value.username shouldEqual updatedTitle

        this.drop(modified.get)
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
