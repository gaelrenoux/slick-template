package psug20180322.util

import java.time.{Instant, LocalDateTime, ZoneOffset}

import slick.jdbc.{PositionedParameters, SetParameter}

/** Additional SetParameter objects: to allow more types inside Slick SQL queries. */
object SetParameters {

  /** Sets an Instant on a query (it will be read as its UTC value and without a timezone) */
  implicit object SetInstant extends SetParameter[Instant] {
    /* Timestamp must contain the local date with UTC offset */
    override def apply(v: Instant, pp: PositionedParameters): Unit = {
      val localDateTime = LocalDateTime.ofInstant(v, ZoneOffset.UTC)
      val t = java.sql.Timestamp.valueOf(localDateTime)
      pp.setTimestamp(t)
    }
  }

}
