package io.comiccloud.repository

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import io.comiccloud.config.DbConfiguration
import io.comiccloud.models.Account
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.{FutureMatchers, OptionMatchers}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterEach

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

class AccountsRepositoryTest(implicit ee: ExecutionEnv) extends Specification
  with DbConfiguration
  with FutureMatchers
  with OptionMatchers
  with BeforeAfterEach {

  sequential
  val timeout: FiniteDuration = 500.milliseconds
  val accounts                = new AccountsRepository(config)
  def before: Unit = {
    Await.result(accounts.init(), timeout)
  }
  def after: Unit = {
    Await.result(accounts.drop(), timeout)
  }

  "Account be inserted successfully" >> {
    val currentTimestamp = Timestamp.valueOf(LocalDateTime.now())
    val account =
      Account(None,
        UUID.randomUUID().toString,
        "Bob",
        "salt",
        "xxxx",
        "Tom",
        Some("Tommyknocker"),
        currentTimestamp)
    accounts.insert(account) must be_==(account.copy(id = Some(1))).awaitFor(timeout)
  }
}
