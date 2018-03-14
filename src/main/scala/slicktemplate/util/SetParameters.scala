package slicktemplate.util

import java.time.{Instant, LocalDateTime, ZoneOffset}

import slicktemplate.model.{Color, Lineage}
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

  implicit object SetLineage extends SetParameter[Lineage] {
    override def apply(lin: Lineage, pp: PositionedParameters): Unit =
      pp.setString(Lineage.LineageToString(lin))
  }

  implicit object SetColor extends SetParameter[Color] {
    override def apply(col: Color, pp: PositionedParameters): Unit =
      pp.setInt(col.hexValue)
  }

}
