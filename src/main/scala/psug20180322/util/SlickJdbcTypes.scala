package psug20180322.util

import java.time.{Instant, LocalDateTime, ZoneOffset}

import slick.jdbc.{JdbcProfile, JdbcType}

import scala.reflect.ClassTag

class SlickJdbcTypes(implicit profile: JdbcProfile) {

  import profile.api._

  implicit val instantWithoutTimezoneType: JdbcType[Instant] = MappedColumnType.base[Instant, java.sql.Timestamp](
    /* DO NOT CONVERT DIRECTLY FROM TIMESTAMP TO INSTANT. The Timestamp is read and written 'as is' when communicating
    with the database, and on the JVM side it is assumed to be in the JVM's local timezone. So, if you store your
    Instants as UTC (as you should), direct conversion will shift the time. */
    i => java.sql.Timestamp.valueOf(LocalDateTime.ofInstant(i, ZoneOffset.UTC)),
    t => t.toLocalDateTime.atZone(ZoneOffset.UTC).toInstant
  )

  implicit val instantWithTimezoneType: JdbcType[Instant] = MappedColumnType.base[Instant, java.sql.Timestamp](
    /* Here the DB driver uses the JVM's timestamp to decide which timezone the timestamp is in, and the conversion
    between Instant and Timestamp assumes the same. This means we should convert directly between those types, not
    specifying any timezone. */
    i => java.sql.Timestamp.from(i),
    t => t.toInstant
  )

  def projected[A, B](mapping: (A, B)*)(implicit tag: ClassTag[A], bType: JdbcType[B]): JdbcType[A] = {
    val aToB = mapping.toMap
    val bToA = mapping.map(_.swap).toMap
    MappedColumnType.base[A, B](
      a => aToB.getOrElse(a, throw new IllegalArgumentException(s"Unknown value $a")),
      b => bToA.getOrElse(b, throw new IllegalArgumentException(s"Unknown mapped value $b"))
    )
  }

}
