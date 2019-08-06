package io.comiccloud.modeling.database

import com.outworkers.phantom.dsl._
import com.outworkers.util.testing._
import io.comiccloud.modeling.entity.Code
import io.comiccloud.utils.{CassandraSpec, CodeDbProvider, CodeGenerator}

import scala.concurrent.Future

class CodeDatabaseTest extends CassandraSpec with CodeGenerator with CodeDbProvider {
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    database.create()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    database.truncate()
  }

  "A Code" should "be inserted into cassandra" in {
    val sample = gen[Code]
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
   * @param code the code to be inserted
   * @return a [[Future]] of [[ResultSet]]
   */
  private def store(code: Code): Future[ResultSet] = CodeDatabase.saveOrUpdate(code)

  /**
   * Utility method to delete into both tables
   *
   * @param code the code to be deleted
   * @return a [[Future]] of [[ResultSet]]
   */
  private def drop(code: Code) = CodeDatabase.delete(code)

}
