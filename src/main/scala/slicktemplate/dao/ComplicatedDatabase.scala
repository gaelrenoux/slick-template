package slicktemplate.dao

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ComplicatedDatabase(val dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) {

  import dbConfig.profile.api._

  /** Runs a Slick action on the master database */
  def onMaster[T](action: DBIOAction[T, _ <: NoStream, Effect.All]): Future[T] =
    dbConfig.db.run(action.transactionally)

  /** Runs a Slick action on a slave database, read-only !*/
  def onSlave[T](action: DBIOAction[T, _ <: NoStream, Effect.Read]): Future[T] =
    dbConfig.db.run(action.transactionally)

  /** Health check on the database */
  def health: Future[Boolean] = dbConfig.db.run(Query(1).result.head.map(_ == 1))

}