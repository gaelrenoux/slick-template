package slicktemplate.model

import java.time.Instant

case class Student(
                    id: Long = 0L,
                    name: String,
                    houseId: Long,
                    lineage: Lineage,
                    updated: Instant = Instant.now
                  )

object Student {

  case class Filter(
                     name: Option[String] = None,
                     houseId: Option[Long] = None,
                     lineage: Option[Lineage] = None
                   )

  object Filter {
    val Empty = Filter()
  }

}