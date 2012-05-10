package com.owlunit.core.ii.mutable.impl

import collection.mutable.{Map => MutableMap}
import com.owlunit.core.ii.mutable.{Recommender, Ii}
import org.neo4j.graphdb.{GraphDatabaseService, Direction, Node}


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private [mutable] trait NeoRecommender extends Recommender with Helpers {

  def graph: GraphDatabaseService

  def compare(a: Ii, b: Ii) = {
    for {
      aNode <- a.node
      bNode <- b.node
    } yield compareMaps(
      getIndirectNodes(aNode, depth),
      getIndirectNodes(bNode, depth)
    )
  } getOrElse 0

  def getSimilar(a: Ii, key: String) = a.loadItems.items match {
    case Some(items) => getSimilar(items, key)
    case None => Map.empty
  }

  def getSimilar(pattern: Map[Ii, Double], key: String, limit: Int) = {
    val candidates = MutableMap[Node, Double]()

    // load parents for query
    for {
      (i, w) <- pattern
      aNode <- i.node
    } {
      val parents = getNodes(aNode, Direction.INCOMING, 1)
      for ((parent, weight) <- parents if parent.hasProperty(key)) candidates.get(parent) match {
        case Some(_) => candidates(parent) += w + weight
        case None    => candidates(parent)  = w + weight
      }
    }

    // compare each parent to query and sort with TreeMap
    // TODO warning for unsaved nodes
    val internalPattern = pattern.map{ case (ii, w) if (ii.node.isDefined) => ii.node.get -> w }
    val result = candidates.map {
      case (parent, weight) => new NeoIi(parent, graph) -> compareMaps(internalPattern, getIndirectNodes(parent, depth))
    }.toList

    result.sortWith((a, b) => a._2 > b._2).take(limit).toMap
  }

  def compareMaps[Node](a: Map[Node, Double], b: Map[Node, Double]): Double = {
    val union = Set[Node]() ++ a.keys ++ b.keys

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