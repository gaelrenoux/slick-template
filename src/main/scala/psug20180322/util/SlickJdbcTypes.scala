package psug20180322.util

import java.time.{Instant, LocalDateTime, ZoneOffset}

import slick.jdbc.{JdbcProfile, JdbcType}

import scala.reflect.ClassTag

class SlickJdbcTypes(implicit profile: JdbcProfile) {

  import profile.api._

  /** When the DB column is "timestamp without time zone" (or just "timestamp", as no time zone is the default) */
  implicit val instantWithoutTimezoneType: JdbcType[Instant] = MappedColumnType.base[Instant, java.sql.Timestamp](
    /* DO NOT CONVERT DIRECTLY FROM TIMESTAMP TO INSTANT. The Timestamp is read and written 'as is' when communicating
    with the database, and on the JVM side it is assumed to be in the JVM's local timezone. So, if you store your
    Instants as UTC (as you should), direct conversion will shift the time. */
    i => java.sql.Timestamp.valueOf(LocalDateTime.ofInstant(i, ZoneOffset.UTC)),
    t => t.toLocalDateTime.atZone(ZoneOffset.UTC).toInstant
  )

  /** When the DB colum is "timestamp with time zone" */
  implicit val instantWithTimezoneType: JdbcType[Instant] = MappedColumnType.base[Instant, java.sql.Timestamp](
    /* Here the DB driver assumes the timestamp is in the JVM's default time zone, and the conversion between Instant
    and Timestamp assumes the same. This means we should convert directly between those types, not specifying any
    timezone. */
    i => java.sql.Timestamp.from(i),
    t => t.toInstant
  )

  def projected[A: ClassTag, B](mapping: (A, B)*)(implicit bType: JdbcType[B]): JdbcType[A] = {
    val aToB = mapping.toMap
    val bToA = mapping.map(_.swap).toMap
    MappedColumnType.base[A, B](
      a => aToB.getOrElse(a, throw new IllegalArgumentException(s"Unknown value $a")),
      b => bToA.getOrElse(b, throw new IllegalArgumentException(s"Unknown mapped value $b"))
    )
  }

}
