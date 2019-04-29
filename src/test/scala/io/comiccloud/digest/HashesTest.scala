package io.comiccloud.digest

import org.scalatest.{FlatSpec, Matchers}

class HashesTest extends FlatSpec with Matchers {
  it should "generate random sha256" in {
    val first = Hashes.randomSha256()
    val second = Hashes.randomSha256()

    first should not be second
  }

  it should "generate random sha1" in {
    val first = Hashes.randomSha1()
    val second = Hashes.randomSha1()

    first should not be second
  }

  it should "generate random md5" in {
    val first = Hashes.randomMd5()
    val second = Hashes.randomMd5()

    first should not be second
  }
}