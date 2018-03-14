package psug20180322.dao

import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, SQLiteProfile}

import scala.concurrent.{ExecutionContext, Future}

class AppDatabase(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  private lazy val api = dbConfig.profile.api

  import api._

  /** Runs a Slick action as a single transaction on the database */
  def apply[T](action: slick.dbio.DBIO[T]): Future[T] =
    dbConfig.db.run(action.transactionally)

  /** Health check on the application database */
  def health: Future[Boolean] = dbConfig.db.run(sql"""select 1""".as[Int].head map (_ == 1))

}

object AppDatabase {
  implicit val profile: SQLiteProfile.type = SQLiteProfile
  implicit val api: SQLiteProfile.API = profile.api
}