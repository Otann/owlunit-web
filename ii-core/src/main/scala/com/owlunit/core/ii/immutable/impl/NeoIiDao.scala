package com.owlunit.core.ii.immutable.impl

import collection.mutable.ListBuffer
import collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb.traversal.Evaluators
import sys.ShutdownHookThread

import com.owlunit.core.ii.immutable.impl.NeoIi.iiToIiImpl
import com.owlunit.core.ii.NotFoundException
import com.owlunit.core.ii.immutable._
import org.neo4j.kernel.{Uniqueness, Traversal}
import org.neo4j.graphdb._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

private[ii] class NeoIiDao(graph: GraphDatabaseService, defaultDepth: Int) extends IiDao {

  private val index = graph.index().forNodes(NeoIiDao.IndexName)
  val recommender = new NeoRecommender(this)

  def init() { ShutdownHookThread { shutdown() } }
  def shutdown() { graph.shutdown() }


  def createIi: Ii = withTx {
    val node = graph.createNode();
    Ii(node);
  }

  def deleteIi(item: Ii) {
    withTx {
      val traverserIterator = Traversal.description()
        .breadthFirst()
        .relationships(NeoIiDao.RelType, Direction.BOTH)
        .evaluator(Evaluators.atDepth(1))
        .traverse(item.node)
        .iterator();

      while (traverserIterator.hasNext) {
        val path = traverserIterator.next()
        path.lastRelationship().delete()
      }

      item.node.delete()
    }
  }

  def load(id: Long) = {
    try {
      Ii(graph.getNodeById(id))
    } catch {
      case ex: org.neo4j.graphdb.NotFoundException => throw new NotFoundException(id, ex)
    }
  };

  def load(key: String, value: String) = {
    val result = ListBuffer[Ii]()
    val iterator = index.get(key, value).iterator
    while (iterator.hasNext) {
      val node = iterator.next()
      result += Ii(node);
    }
    result.toList
  }


  def search(key: String, queue: String): Seq[Ii] = {
    val result = ListBuffer[Ii]()
    val iterator = index.query(key, queue).iterator()
    while (iterator.hasNext)
      result += Ii(iterator.next())

    result.toList
  }

  def setMeta(item: Ii, key: String, value: String) = setMetaExtended(item, key, value, true)

  def setMetaUnindexed(item: Ii, key: String, value: String) = setMetaExtended(item, key, value, false)

  private def setMetaExtended(item: Ii,  key: String, value: String, isIndexed: Boolean):Ii = withTx {
    item.node.setProperty(key, value)

    try {
      index.remove(item.node, key);
    } catch {
      // That's ok, it possibly was unindexed
      case e: org.neo4j.graphdb.NotFoundException =>
    }

    if (isIndexed)
      index.add(item.node, key, value)

    item.meta match {
      case Some(x) => item.copy(meta = Some(x + (key -> value)))
      case None => item
    }
  }

  def removeMeta(item: Ii, key: String) = withTx {
    item.node.removeProperty(key)

    index.remove(item.node, key)

    item.meta match {
      case Some(x) => item.copy(meta = Some((x - key)))
      case None => item
    }
  }

  def setComponentWeight(item: Ii, component: Ii, weight: Double): Ii = withTx {
    val rel = NeoIiDao.getRelation(item.node, component.node) match {
      case Some(x) => x
      case None => item.node.createRelationshipTo(component.node, NeoIiDao.RelType)
    }
    rel.setProperty(NeoIiDao.WeightPropertyName, weight)

    item.components match {
      case Some(x) => item.copy(components = Some(x + (component -> weight)))
      case None => item
    }
  }

  def removeComponent(item: Ii, component: Ii): Ii = withTx {
    NeoIiDao.getRelation(item.node, component.node) match {
      case Some(x) => x.delete()
      case None => ()
    }

    item.components match {
      case Some(x) => item.copy(components = Some(x - component))
      case None => item
    }
  }

  def loadMeta(item: Ii): Ii = {

    val meta = MutableMap[String, String]()
    val metaIterator = item.node.getPropertyKeys.iterator()

    while (metaIterator.hasNext) {
      val key = metaIterator.next()
      meta.put(key, item.node.getProperty(key).toString)
    }

    item.copy(meta = Some(meta.toMap))
  }

  def loadComponents(item: Ii): Ii = {
    val nodes = getNodes(item.node, Direction.OUTGOING, 1)
    val items = nodes.map {case (n, w) => Ii(n) -> w}
    item.copy(components = Some(items))
  }


  def loadParents(item: Ii): Ii = {
    val nodes = getNodes(item.node, Direction.INCOMING, 1)
    val items = nodes.map {case (n, w) => Ii(n) -> w}
    item.copy(parents = Some(items))
  }

  def getIndirectComponents(item: Ii) = getIndirectNodes(item.node).map{case (n, w) => Ii(n) -> w}

  private[ii] def getIndirectNodes(node: Node):Map[Node,  Double] = getNodes(node, Direction.OUTGOING, defaultDepth)

  private[ii] def getNodes(start: Node, direction: Direction, depth: Int): Map[Node, Double] = {

    val nodes = MutableMap[Node, Double]()

    if (depth == 1) {
      val relsIterator = start.getRelationships(NeoIiDao.RelType, direction).iterator()
      while (relsIterator.hasNext) {
        val rel = relsIterator.next()
        val n = rel.getOtherNode(start)
        val w = rel.getProperty(NeoIiDao.WeightPropertyName).asInstanceOf[Double]

        nodes += (n -> w)
      }
    } else {

      val traverserIterator = Traversal.description()
        .breadthFirst()
        .relationships(NeoIiDao.RelType, direction)
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
          val w = rel.getProperty(NeoIiDao.WeightPropertyName).asInstanceOf[Double]
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

  def withTx[T <: Any](operation: => T): T = {
    val tx = synchronized {
      graph.beginTx()
    }
    try {
      val ret = operation
      tx.success()
      ret
    } finally {
      tx.finish()
    }
  }

}

private[ii] object NeoIiDao {

  private val IndexName = "ITEMS_INDEX"
  private val WeightPropertyName = "WEIGHT"

  private object RelType extends RelationshipType {
    def name() = "CONNECTED"
  }

  private def getRelation(a: Node, b: Node): Option[Relationship] = {
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

}
