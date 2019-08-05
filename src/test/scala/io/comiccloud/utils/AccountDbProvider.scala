package io.comiccloud.utils

import com.outworkers.phantom.database.DatabaseProvider
import io.comiccloud.modeling.database.AccountDatabase
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

trait AccountDbProvider extends DatabaseProvider[AccountDatabase] {
  override def database: AccountDatabase = AccountDatabase
}

trait CassandraSpec
    extends FlatSpec
    with Matchers
    with Inspectors
    with ScalaFutures
    with OptionValues
    with BeforeAndAfterAll
    with AccountDbProvider
