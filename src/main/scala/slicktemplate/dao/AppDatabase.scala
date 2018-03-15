package slicktemplate.dao

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class AppDatabase(val dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {

  import dbConfig.profile.api._

  /** Runs a Slick action as a single transaction on the database */
  def apply[T](action: slick.dbio.DBIO[T]): Future[T] =
    dbConfig.db.run(action.transactionally)

  /** Health check on the database */
  def health: Future[Boolean] = dbConfig.db.run(Query(1).result.head.map(_ == 1))

}