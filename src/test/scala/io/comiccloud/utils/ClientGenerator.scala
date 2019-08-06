package io.comiccloud.utils

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.util.samplers.Sample
import com.outworkers.util.testing.{ShortString, gen}
import io.comiccloud.modeling.entity
import io.comiccloud.modeling.entity.Client
import org.joda.time.{DateTime, DateTimeZone}

trait ClientGenerator {
  implicit object ClientGenerator extends Sample[Client] {
    override def sample: entity.Client = {
      entity.Client(
        UUIDs.timeBased(),
        UUIDs.timeBased(),
        UUIDs.timeBased(),
        None,
        "authorization_code",
        new DateTime(DateTimeZone.UTC)
      )
    }
  }

}
