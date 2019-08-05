package io.comiccloud.digest

import org.specs2.mutable.Specification

class IDTest extends Specification {

  val id = new ID()

  "IDTest" should {
    "next" in {
      id.next()
      ok
    }
  }
}
