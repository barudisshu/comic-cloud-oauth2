package io.comiccloud.utils

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.util.samplers.Sample
import com.outworkers.util.testing.{ShortString, gen}
import io.comiccloud.modeling.entity
import io.comiccloud.modeling.entity.Code
import org.joda.time.{DateTime, DateTimeZone}

trait CodeGenerator {
  implicit object CodeGenerator extends Sample[Code] {
    override def sample: entity.Code = {
      entity.Code(
        UUIDs.timeBased(),
        UUIDs.timeBased(),
        UUIDs.timeBased(),
        gen[ShortString].value,
        None,
        new DateTime(DateTimeZone.UTC)
      )
    }
  }

}
