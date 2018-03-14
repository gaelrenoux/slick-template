package psug20180322.dao

import slick.sql.{SqlAction, SqlStreamingAction}

class Queries {

  // Bring the Slick DSL into scope
  import AppDatabase.api._

  val countInHouses: DBIOAction[Vector[(String, Long)], Streaming[(String, Long)], Effect.Read] =
    sql"""
        select h.name, count(*)
        from student s
        join house h on h.id = s.house_id
        group by h.name
      """.as[(String, Long)]

  val resetPoints: SqlAction[Int, NoStream, Effect.Write] =
    sqlu"""
           update house set points = 0
      """
}
