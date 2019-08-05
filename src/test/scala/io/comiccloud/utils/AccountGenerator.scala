package io.comiccloud.utils

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.util.samplers.Sample
import com.outworkers.util.testing._
import io.comiccloud.modeling.entity
import io.comiccloud.modeling.entity.Account
import org.joda.time.{DateTime, DateTimeZone}

trait AccountGenerator {
  implicit object SongGenerator extends Sample[Account] {
    override def sample: entity.Account = {
      entity.Account(
        UUIDs.timeBased(),
        gen[FullName].value,
        gen[ShortString].value,
        gen[ShortString].value,
        gen[EmailAddress].value,
        None,
        new DateTime(DateTimeZone.UTC)
      )
    }
  }
}
