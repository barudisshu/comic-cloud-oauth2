package io.comiccloud.utils

import com.outworkers.phantom.database.DatabaseProvider
import io.comiccloud.modeling.database._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

trait AccountDbProvider extends DatabaseProvider[AccountDatabase] {
  override def database: AccountDatabase = AccountDatabase
}

trait ClientDbProvider extends DatabaseProvider[ClientDatabase] {
  override def database: ClientDatabase = ClientDatabase
}

trait CodeDbProvider extends DatabaseProvider[CodeDatabase] {
  override def database: CodeDatabase = CodeDatabase
}

trait TokenDbProvider extends DatabaseProvider[TokenDatabase] {
  override def database: TokenDatabase = TokenDatabase
}

trait CassandraSpec
    extends FlatSpec
    with Matchers
    with Inspectors
    with ScalaFutures
    with OptionValues
    with BeforeAndAfterAll
