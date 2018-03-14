package slicktemplate.util

import java.time.{Instant, ZoneOffset}

import slicktemplate.model.{Color, Lineage}
import slick.jdbc.{GetResult, PositionedResult}

/** Additional GetResult objects: to convert plain SQL queries results to various types, using the as[T] operation. */
object GetResults {

  /** Gets an Instant from a query. The Instant is assumed to be stored in the result set in UTC value, without a
    * timezone (which is the default on all SQL databases). */
  implicit object GetInstant extends GetResult[Instant] {
    /* The JDBC driver returns a Timestamp with the local time as it was in the DB, which means it must be understood as UTC. */
    def apply(rs: PositionedResult): Instant = rs.nextTimestamp().toLocalDateTime.atZone(ZoneOffset.UTC).toInstant
  }

  implicit object GetColor extends GetResult[Color] {
    def apply(rs: PositionedResult): Color = Color(rs.nextInt())
  }

  implicit object GetLineage extends GetResult[Lineage] {
    def apply(rs: PositionedResult): Lineage = Lineage.StringToLineage(rs.nextString())
  }

}
