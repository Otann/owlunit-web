package com.owlunit.core.ii.immutable.impl

import collection.mutable.{Set => MutableSet, Map => MutableMap}
import org.neo4j.graphdb.{Direction, Node}
import com.owlunit.core.ii.immutable.{IiDao, Recommender, Ii}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private [ii] class NeoRecommender(dao: NeoIiDao) extends Recommender {
  import NeoRecommender.compareMaps
  import NeoIi.iiToIiImpl

  def compare(a: Ii, b: Ii) = compareMaps(
    dao.getIndirectNodes(a.node),
    dao.getIndirectNodes(b.node)
  )

  def getSimilar(a: Ii, key: String) = a.components match {
    case Some(components) => getSimilar(components, key)
    case None => Map.empty
  }

  def getSimilar(pattern: Map[Ii, Double], key: String, limit: Int) = {
    val candidates = MutableMap[Node, Double]()

    // load parents for query
    for ((i, w) <- pattern) {
      val parents = dao.getNodes(i.node, Direction.INCOMING, 1)
      for ((parent, weight) <- parents if parent.hasProperty(key)) candidates.get(parent) match {
        case Some(_) => candidates(parent) += w + weight
        case None    => candidates(parent)  = w + weight
      }
    }

    // compare each parent to query and sort with TreeMap
    val internalPattern = pattern.map{ case (ii, w) => ii.node -> w }
    val result = candidates.map {
          case (parent, weight) => Ii(parent) -> compareMaps(internalPattern, dao.getIndirectNodes(parent))
    }.toList

    result.sortWith((a, b) => a._2 > b._2).take(limit).toMap
  }
}

object NeoRecommender {

  def compareMaps[Node](a: Map[Node, Double], b: Map[Node, Double]): Double = {
    val union = MutableSet[Node]() ++ a.keys ++ b.keys

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
