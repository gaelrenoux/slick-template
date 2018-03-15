package slicktemplate.dao

import java.time._

import slick.jdbc.{JdbcProfile, JdbcType}
import slick.sql.SqlAction
import slicktemplate.model.{House, Lineage, Student}
import slicktemplate.util.SlickJdbcTypes

import scala.concurrent.ExecutionContext

/* Having vals are necessary, try to remove them and see */
class StudentDao(val houseDao: HouseDao, val profile: JdbcProfile)(implicit ec: ExecutionContext) {

  // Bring the Slick DSL into scope
  import profile.api._

  private val moreJdbcTypes = new SlickJdbcTypes

  import moreJdbcTypes.instantWithoutTimezoneType

  private implicit val lineageType: JdbcType[Lineage] = moreJdbcTypes.projected(Lineage.LineageToString.toSeq: _*)

  private[dao] class StudentTable(tag: Tag) extends Table[Student](tag, "STUDENT") {
    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)

    def name = column[String]("NAME")

    def houseId = column[Long]("HOUSE_ID")

    def lineage = column[Lineage]("LINEAGE")

    def updated = column[Instant]("UPDATED")

    def fkHouseId = foreignKey("FK_STUDENT_HOUSE_ID", houseId, houseDao.table)(_.id, onDelete = ForeignKeyAction.Cascade)

    def * = (id, name, houseId, lineage, updated) <>
      ((Student.apply _).tupled, Student.unapply)
  }


  private[dao] val table = TableQuery[StudentTable]

  private val tableReturningTable = table returning table

  val createTable: DBIOAction[Unit, NoStream, Effect.Schema] = table.schema.create

  def add(s: Student): DBIOAction[Student, NoStream, Effect.Write] =
    tableReturningTable += s.copy(updated = Instant.now())

  def addAll(seq: Iterable[Student]): DBIOAction[Seq[Student], NoStream, Effect.Write] =
    tableReturningTable ++= seq.map(_.copy(updated = Instant.now()))

  def get(id: Long): DBIOAction[Option[Student], NoStream, Effect.Read] =
    table.filter(_.id === id).result.headOption

  def delete(id: Long): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.filter(_.id === id).delete.map(_ > 0)

  def update(student: Student): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.filter(_.id === student.id).update(student).map(_ > 0)

  def upsert(student: Student): DBIOAction[Boolean, NoStream, Effect.Write] =
    table.insertOrUpdate(student.copy(updated = Instant.now())).map(_ > 0)

  def getWithHouse(id: Long): SqlAction[Option[(Student, House)], NoStream, Effect.Read] = {
    table.filter(_.id === id).join(houseDao.table).on(_.houseId === _.id).result.headOption
  }

  /** This one is slower than getWithHouse, because monadic joins (using flatMap) are not as fast as applicative joins */
  def ineffectiveGetWithHouse(id: Long): DBIOAction[Option[(Student, House)], NoStream, Effect.Read] = {
    //TODO don't use this unless you have to
    val query = for {
      student <- table.filter(_.id === id)
      house <- student.fkHouseId
    } yield (student, house)
    query.result.headOption
  }

  def countByHouse: DBIOAction[Seq[(Long, Int)], NoStream, Effect.Read] =
    table.groupBy(_.houseId).map { case (hId, studentsQuery) => (hId, studentsQuery.length) }.result

  def list(filter: Student.Filter = Student.Filter.Empty): DBIOAction[Seq[Student], Streaming[Student], Effect.Read] =
    withFilter(filter).sortBy(_.id.desc).result

  def find(filter: Student.Filter = Student.Filter.Empty): DBIOAction[Option[Student], NoStream, Effect.Read] =
    withFilter(filter).result.headOption

  def deleteAll(filter: Student.Filter = Student.Filter.Empty): DBIOAction[Int, NoStream, Effect.Write] =
    withFilter(filter).delete

  private def withFilter(filter: Student.Filter) = {
    var query: Query[StudentTable, Student, Seq] = table
    filter.name foreach { n => query = query.filter(_.name === n) }
    filter.houseId foreach { hid => query = query.filter(_.houseId === hid) }
    filter.lineage foreach { lin => query = query.filter(_.lineage === lin) }
    query
  }

}
