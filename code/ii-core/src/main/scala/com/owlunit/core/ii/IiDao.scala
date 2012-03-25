package com.owlunit.core.ii

import com.owlunit.core.ii.Ii.IiImpl.iiToIiImpl
import collection.mutable.ListBuffer
import collection.mutable.{Map => MutableMap}
import org.neo4j.graphdb.{Direction, RelationshipType, Relationship, Node}
import org.neo4j.graphdb.traversal.Evaluators
import org.neo4j.kernel.{Uniqueness, Traversal, EmbeddedGraphDatabase}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiDao {

  def createIi: Ii
  def deleteIi(item: Ii)

  def load(id: Long): Ii
  def load(key: String, value: String): Seq[Ii]
  def search(key: String, queue: String): Seq[Ii]

  def setMeta(item: Ii, key: String, value: String): Ii
  def setMetaUnindexed(item: Ii, key: String, value: String): Ii
  def removeMeta(item: Ii, key: String): Ii

  def setComponentWeight(item: Ii, component: Ii, weight: Double): Ii
  def removeComponent(item: Ii, component: Ii): Ii

  def loadMeta(item: Ii): Ii
  def loadComponents(item: Ii): Ii
  def loadParents(item: Ii): Ii

  def getIndirectComponents(item: Ii): Map[Ii, Double]

}

object IiDao {

  val DefaultDepth = 3

  def apply(path: String, depth: Int = DefaultDepth): IiDao = new IiDaoImpl(path, depth)

  private object IiDaoImpl {

    private val IndexName = "ITEMS_INDEX"
    private val WeightPropertyName = "WEIGHT"

    private object RelType extends RelationshipType {
      def name() = "CONNECTED"
    }

    private def getRelation(a: Node, b: Node):Option[Relationship] = {
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

  private class IiDaoImpl(path: String, depth: Int) extends IiDao {

    private val graph = new EmbeddedGraphDatabase(path)
    private val index = graph.index().forNodes(IiDaoImpl.IndexName)

    def createIi: Ii = withTx {
      val node = graph.createNode();
      Ii(node);
    }

    def deleteIi(item: Ii) {
      withTx {
        val traverserIterator = Traversal.description()
          .breadthFirst()
          .relationships(IiDaoImpl.RelType, Direction.BOTH)
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
      val rel = IiDaoImpl.getRelation(item.node, component.node) match {
        case Some(x) => x
        case None => item.node.createRelationshipTo(component.node, IiDaoImpl.RelType)
      }
      rel.setProperty(IiDaoImpl.WeightPropertyName, weight)

      item.components match {
        case Some(x) => item.copy(components = Some(x + (component -> weight)))
        case None => item
      }
    }

    def removeComponent(item: Ii, component: Ii): Ii = withTx {
      IiDaoImpl.getRelation(item.node, component.node) match {
        case Some(x) => x.delete()
        case None => ()
      }

      item.components match {
        case Some(x) => item.copy(components = Some(x - component))
        case None => item
      }
    }

    def loadMeta(item: Ii): Ii = {
      import scala.collection.JavaConverters._
      
      val meta = MutableMap[String, String]()
      val metaIterator = item.node.getPropertyKeys.iterator()
      
      for (key <- item.node.getPropertyKeys.asScala)
        yield (key -> item.node.getProperty(key).toString)

      
      while (metaIterator.hasNext) {
        val key = metaIterator.next()
        meta.put(key, item.node.getProperty(key).toString)
      }

      item.copy(meta = Some(meta.toMap))
    }

    def loadComponents(item: Ii): Ii = {
      val items = MutableMap[Ii, Double]()

      val relsIterator = item.node.getRelationships(IiDaoImpl.RelType, Direction.OUTGOING).iterator()
      while (relsIterator.hasNext) {
        val rel = relsIterator.next()
        val n = rel.getEndNode
        val w = rel.getProperty(IiDaoImpl.WeightPropertyName).asInstanceOf[Double]

        items + (Ii(n) -> w)
      }

      item.copy(components = Some(items.toMap))
    }


    def loadParents(item: Ii): Ii = {
      val items = MutableMap[Ii, Double]()

      val relsIterator = item.node.getRelationships(IiDaoImpl.RelType, Direction.INCOMING).iterator()
      while (relsIterator.hasNext) {
        val rel = relsIterator.next()
        val n = rel.getEndNode
        val w = rel.getProperty(IiDaoImpl.WeightPropertyName).asInstanceOf[Double]

        items + (Ii(n) -> w)
      }

      item.copy(parents = Some(items.toMap))
    }

    def getIndirectComponents(item: Ii) = {
      val nodes = MutableMap[Node, Double]()

      val traverserIterator = Traversal.description()
        .breadthFirst()
        .relationships(IiDaoImpl.RelType, Direction.OUTGOING)
        .uniqueness(Uniqueness.NODE_PATH)
        .evaluator(Evaluators.excludeStartPosition())
        .evaluator(Evaluators.toDepth(depth))
        .traverse(item.node)
        .iterator()

      while (traverserIterator.hasNext) {
        val path = traverserIterator.next()

        var weight = 0.0
        var qualifier = 1

        val relIterator = path.relationships().iterator()
        while (relIterator.hasNext) {
          val rel = relIterator.next()
          val w = rel.getProperty(IiDaoImpl.WeightPropertyName).asInstanceOf[Double]
          weight += w / qualifier
          qualifier <<= 1
        }

        val node = path.endNode()
        nodes get node match {
          case Some(x) => nodes(node) = x + weight
          case None => nodes(node) = weight
        }

      }

      val result = MutableMap[Ii, Double]()
      for ((node, weight) <- nodes) {
        result + (Ii(node) -> weight)
      }

      result.toMap
    }

    private def withTx(operation: => Ii):Ii = {
      val tx = graph.beginTx()
      try {
        val result = operation
        tx.success()
        result
      } catch {
        case ex:Exception => { tx.failure(); throw new DAOException("Exception handling transaction", ex) }
      }
    }

    private def withTx(operation: => Unit) {
      val tx = graph.beginTx()
      try {
        operation
        tx.success()
      } catch {
        case ex:Exception => { tx.failure(); throw new DAOException("Exception handling transaction", ex) }
      }
    }

  }

}
