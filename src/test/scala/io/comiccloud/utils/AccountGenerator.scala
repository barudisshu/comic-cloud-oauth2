package io.comiccloud.utils

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.util.samplers.Sample
import com.outworkers.util.testing._
import io.comiccloud.modeling.entity
import io.comiccloud.modeling.entity.Account
import org.joda.time.{DateTime, DateTimeZone}

trait AccountGenerator {
  implicit object AccountGenerator extends Sample[Account] {
    override def sample: entity.Account = {
      entity.Account(
        UUIDs.timeBased(),
        "Galudisu",
        "123",
        gen[ShortString].value,
        gen[EmailAddress].value,
        None,
        new DateTime(DateTimeZone.UTC)
      )
    }
  }
}
