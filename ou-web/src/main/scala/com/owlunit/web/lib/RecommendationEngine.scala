package com.owlunit.web.lib

import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.model.common.IiTagRecord
import com.owlunit.web.model.Movie

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object RecommendationEngine {

  def iiDao = DependencyFactory.iiDao.vend

  def recommend(query: Seq[IiTagRecord[_]]): Seq[Movie] = {
    query.filter(_.kind == "movie").map(_.asInstanceOf[Movie])
  }

}
