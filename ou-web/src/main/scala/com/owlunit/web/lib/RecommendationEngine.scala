package com.owlunit.web.lib

import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.model.common.IiTagRecord
import com.owlunit.web.model.Movie
import com.owlunit.core.ii.mutable.Ii
import com.foursquare.rogue.Rogue._
import net.liftweb.common.Loggable

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object RecommendationEngine extends Loggable {

  def iiDao = DependencyFactory.iiDao.vend

  def recommend(querySet: Seq[IiTagRecord[_]]): Map[Movie, Double] = {
    logger.debug("-" * 20)
    logger.debug("query set size: %s" format querySet.size)

    val query = collection.mutable.Map[Ii, Double]()

    for (record <- querySet) {
      query(record.ii) = 10
      val items = iiDao.indirectComponents(record.ii, 1)
      for ((key, value) <- items) {
        query.get(key) match {
          case Some(w) => query(key) = w + value
          case None    => query(key) = value
        }
      }
    }
    logger.debug("indirect set size: %s" format query.size)

    val similarIis = iiDao.getSimilar(query.toMap, "ii.cinema.movie.Name", limit = 30)
    val similarResults = similarIis -- querySet.map(_.ii)

    val movieRecordQuery = Movie where (_.informationItemId in similarResults.keys.map(_.id))
    val result = movieRecordQuery.fetch().map(m => (m -> similarResults(m.ii))).toMap

    logger.debug("recommendation ended")
    result
  }

}
