package slicktemplate.dao

import slick.basic.DatabaseConfig
import slick.jdbc.{H2Profile, HsqldbProfile, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class AppDatabase(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {
  private lazy val api = dbConfig.profile.api

  import api._

  private val d = Database.forConfig("h2db.db")

  /** Runs a Slick action as a single transaction on the database */
  def apply[T](action: slick.dbio.DBIO[T]): Future[T] =
    dbConfig.db.run(action.transactionally)

  /** Health check on the database */
  def health: Future[Boolean] = dbConfig.db.run(Query(1).result.head.map(_ == 1))

}

object AppDatabase {
  implicit val profile: HsqldbProfile.type = HsqldbProfile
  implicit val api: HsqldbProfile.API = profile.api
}