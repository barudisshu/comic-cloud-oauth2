package io.comiccloud.utils

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.util.samplers.Sample
import com.outworkers.util.testing.{ShortString, gen}
import io.comiccloud.modeling.entity
import io.comiccloud.modeling.entity.Token
import org.joda.time.{DateTime, DateTimeZone}

trait TokenGenerator {
  implicit object TokenGenerator extends Sample[Token] {
    override def sample: entity.Token = {
      entity.Token(
        UUIDs.timeBased(),
        UUIDs.timeBased(),
        gen[ShortString].value,
        gen[ShortString].value,
        new DateTime(DateTimeZone.UTC)
      )
    }
  }

}
