package com.owlunit.web.lib

import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.model.common.IiTagRecord
import com.owlunit.web.model.{User, Movie}
import com.owlunit.core.ii.mutable.Ii
import com.foursquare.rogue.Rogue._
import net.liftweb.common.{Loggable, Full}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object RecommendationEngine extends Loggable {

  def iiDao = DependencyFactory.iiDao.vend

  def recommend(querySet: Seq[IiTagRecord[_]]): Seq[Movie] = {
    val weightedQuery: Map[Ii, Double] = querySet.map(t => (t.ii -> 1.0)).toMap
    logger.debug("weightedQuery" + weightedQuery)

    val iiResults = iiDao.getSimilar(weightedQuery, "ii.cinema.movie.Name").keys.map(_.id)
    logger.debug("iiResults" + iiResults)

    val movieQuery = Movie where (_.informationItemId in iiResults)
    movieQuery.fetch()

//    querySet.filter(_.kind == "movie").map(_.asInstanceOf[Movie])
  }

}
