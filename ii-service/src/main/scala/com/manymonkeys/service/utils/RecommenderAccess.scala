package com.manymonkeys.service.utils

import reflect.BeanProperty
import org.springframework.beans.factory.annotation.Autowired
import com.manymonkeys.core.algo.Recommender
import com.manymonkeys.core.ii.{IiDao, Ii}
import scalaj.collection.Imports._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait RecommenderAccess {

  @BeanProperty
  @Autowired
  var recommender: Recommender = null

  def recommend(item: Ii, dao: IiDao, filter: (Ii => Boolean)): Map[Ii, Double] = {
    val recommenderResults = recommender.getMostLike(item, dao).asScala
    recommenderResults.map({case (item, value) if filter(item) => (item,  value.doubleValue())}).toMap
  }

}