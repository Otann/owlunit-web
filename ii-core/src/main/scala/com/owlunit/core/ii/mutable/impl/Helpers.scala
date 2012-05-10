package com.owlunit.core.ii.mutable.impl

import collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb._
import org.neo4j.kernel.{Uniqueness, Traversal}
import traversal.Evaluators
import com.owlunit.core.ii.mutable.Ii

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */



trait Helpers {

  private[impl] val IndexName = "ITEMS_INDEX"
  private[impl] val WeightPropertyName = "WEIGHT"

  private[impl] def getNodes(start: Node, direction: Direction, depth: Int): Map[Node, Double] = {

    val nodes = collection.mutable.Map[Node, Double]()

    if (depth == 1) {
      val relsIterator = start.getRelationships(RelType, direction).iterator()
      while (relsIterator.hasNext) {
        val rel = relsIterator.next()
        val n = rel.getOtherNode(start)
        val w = rel.getProperty(WeightPropertyName).asInstanceOf[Double]

        nodes += (n -> w)
      }
    } else {

      val traverserIterator = Traversal.description()
        .breadthFirst()
        .relationships(RelType, direction)
        .uniqueness(Uniqueness.NODE_PATH)
        .evaluator(Evaluators.excludeStartPosition())
        .evaluator(Evaluators.toDepth(depth))
        .traverse(start)
        .iterator()

      while (traverserIterator.hasNext) {
        val path = traverserIterator.next()

        var weight = 0.0
        var qualifier = 1

        val relIterator = path.relationships().iterator()
        while (relIterator.hasNext) {
          val rel = relIterator.next()
          val w = rel.getProperty(WeightPropertyName).asInstanceOf[Double]
          weight += w / qualifier
          qualifier <<= 1
        }

        val node = path.endNode()
        nodes get node match {
          case Some(x) => nodes(node) = x + weight
          case None => nodes(node) = weight
        }

      }
    }

    nodes.toMap
  }

  private[impl] def getIndirectNodes(node: Node, depth: Int):Map[Node,  Double] = getNodes(node, Direction.OUTGOING, depth)

  private[impl] def getRelation(a: Node, b: Node): Option[Relationship] = {
    val aIter = a.getRelationships(RelType).iterator()
    val bIter = b.getRelationships(RelType).iterator()

    while (aIter.hasNext && bIter.hasNext) {
      val aRel = aIter.next()
      if (aRel.getEndNode == b)
        return Some(aRel)

      val bRel = bIter.next()
      if (bRel.getEndNode == b)
        return Some(bRel)
    }

    None
  }

  implicit def iiToIiImpl(item: Ii): NeoIi =
    if (item.isInstanceOf[NeoIi])
      item.asInstanceOf[NeoIi]
    else
      throw new IllegalArgumentException("This dao can not operate with item %s" format item.toString)

}

