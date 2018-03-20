package slicktemplate.dao

import slick.jdbc.{JdbcProfile, JdbcType}
import slick.sql.FixedSqlStreamingAction
import slicktemplate.model.{Color, House}

import scala.concurrent.ExecutionContext

class HouseDao(val profile: JdbcProfile)(implicit ec: ExecutionContext) {

  // Bring the Slick DSL into scope
  import profile.api._


  private implicit val colorJdbcType: JdbcType[Color] =
    MappedColumnType.base[Color, Int](
      c => c.hexValue,
      i => Color(i)
    )

  private[dao] class HouseTable(tag: Tag) extends Table[House](tag, "HOUSE") {
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)

    def name = column[String]("NAME")

    def primaryColor = column[Color]("PRIMARY_COLOR")

    def secondaryColor = column[Color]("SECONDARY_COLOR")

    def points = column[Long]("POINTS")

    def * = (id, name, primaryColor, secondaryColor, points) <>
      ((House.apply _).tupled, House.unapply)
  }

  private[dao] val table = TableQuery[HouseTable]

  private val tableReturningTable = table returning table

  private[dao] def qGet(hid: Long): Query[HouseTable, House, Seq] = table.filter(_.id === hid)

  val createTable: DBIOAction[Unit, NoStream, Effect.Schema] = table.schema.create

  def add(h: House): DBIOAction[House, NoStream, Effect.Write] =
    tableReturningTable += h

  def addAll(seq: Iterable[House]): DBIOAction[Seq[House], NoStream, Effect.Write] =
    tableReturningTable ++= seq

  def get(hid: Long): DBIO[Option[House]] =
    qGet(hid).result.headOption

  def delete(id: Long): DBIOAction[Boolean, NoStream, Effect.Write] =
    qGet(id).delete.map(_ > 0)

  def update(house: House): DBIOAction[Boolean, NoStream, Effect.Write] =
    qGet(house.id).update(house).map(_ > 0)

  def getTop: FixedSqlStreamingAction[Seq[String], String, Effect.Read] =
    table.filter(_.points > 0L).sortBy(_.points.desc).map(_.name).result

  /** Returns true if it already existed */
  def upsert(house: House): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.insertOrUpdate(house).map(_ > 0)

  def list(filter: House.Filter = House.Filter.Empty): DBIOAction[Seq[House], Streaming[House], Effect.Read] =
    withFilter(filter).sortBy(_.id.desc).result

  def find(filter: House.Filter): DBIOAction[Option[House], NoStream, Effect.Read] =
    withFilter(filter).result.headOption

  def deleteAll(filter: House.Filter): DBIOAction[Int, NoStream, Effect.Write] =
    withFilter(filter).delete

  private def withFilter(filter: House.Filter) = {
    var query: Query[HouseTable, House, Seq] = table
    filter.name foreach { n => query = query.filter(_.name === n) }
    filter.anyColor foreach { color => query = query.filter(h => h.primaryColor === color || h.secondaryColor === color) }
    query
  }

}
