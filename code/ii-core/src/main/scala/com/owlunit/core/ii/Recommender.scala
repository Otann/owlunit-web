package com.owlunit.core.ii

import collection.mutable.{Set => MutableSet, Map => MutableMap}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object Recommender {

  def compareMaps(a: Map[Ii, Double], b: Map[Ii, Double]): Double = {
    val union = MutableSet[Ii]() ++ a.keys ++ b.keys

    if (union.size == 0)
      return 0.0

    val aOverall = a.values.foldLeft(0.0)(_ + _)
    val bOverall = b.values.foldLeft(0.0)(_ + _)

    var min = 0.0

    for (item <- union) {
      val aWeight = a.getOrElse(item, 0.0) / aOverall
      val bWeight = b.getOrElse(item, 0.0) / bOverall

      min += aWeight min bWeight;
    }

    min * 100
  }

}


class Recommender(dao: IiDao) {

  def compareItems(a: Ii, b: Ii): Double = Recommender.compareMaps(
    dao.getIndirectComponents(a),
    dao.getIndirectComponents(b))

  def getMostLike(a: Ii): Map[Ii,  Double] = {
    getMostLike(a.components.get) - a -- a.components.get.keys
  }

  //TODO Anton Chebotaev - IMPLEMENT
  def getMostLike(map: Map[Ii,  Double]): Map[Ii,  Double] = map

}