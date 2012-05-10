package com.owlunit.core.ii.mutable.impl

import com.owlunit.core.ii.mutable.Ii
import org.neo4j.graphdb._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


private[impl] class NeoIi(var node: Option[Node], graph: GraphDatabaseService) extends Ii with Helpers {

  def this(node: Node, graph: GraphDatabaseService) = this(Some(node), graph)

  def index = graph.index().forNodes(IndexName)

  def id = node.map(_.getId).getOrElse(0)

  var meta: Option[Map[String, String]] = None
  var items: Option[Map[Ii, Double]] = None

  var removedMeta = Map[String, String]()

  def save:Ii = {

    val tx = graph.beginTx()
    try {

      if (this.node.isEmpty) {
        this.node = Some(graph.createNode())
      }

      for {
        n <- this.node
        metaMap <- meta
        (key, value) <- metaMap
      } {
        n.setProperty(key, value)
        index.add(n, key, value)
      }

      for {
        itemsMap <- items
        (item: NeoIi, weight) <- itemsMap
        thisNode <- this.node
        thatNode <- item.node
      } {
        val rel = getRelation(thisNode, thatNode) match {
          case Some(relation) => relation
          case None => thisNode.createRelationshipTo(thatNode, RelType)
        }
        rel.setProperty(WeightPropertyName, weight)
      }

      tx.success()

      this
    } finally {
      tx.finish()
    }

  }

  def delete() {

    val tx = graph.beginTx()
    try {

      if (this.node.isEmpty) {
        return
      }

      for {
        n <- this.node
      } {
        index.remove(n)
        val rels = n.getRelationships.iterator()
        while (rels.hasNext) {
          rels.next().delete()
        }
        
        n.delete()
      }

      tx.success()

      this
    } finally {
      tx.finish()
    }

  }

  def loadMeta = {
    if (meta.isEmpty) {
      meta = this.node match {
        case None           => Some(Map())
        case Some(thisNode) => Some(getMeta(thisNode))
      }
    }
    this
  }

  private def getMeta(node: Node): Map[String, String] = {
    import collection.mutable.Map

    val newMeta = Map[String, String]()
    val iterator = node.getPropertyKeys.iterator()

    while (iterator.hasNext) {
      val key = iterator.next()
      newMeta.put(key, node.getProperty(key).toString)
    }
    newMeta.toMap
  }

  def loadItems = {
    if (items.isEmpty) {
      items = this.node match {
        case None           => Some(Map())
        case Some(thisNode) => Some(getItems(thisNode))
      }
    }
    this
  }
  
  private def getItems(node: Node): Map[Ii, Double] = {
    val nodes = getNodes(node, Direction.OUTGOING, 1)
    nodes.map {case (n, w) => new NeoIi(Some(n), graph) -> w}
  }

  def setMeta(key: String, value: String) = {
    loadMeta
    meta = meta.map(_ + (key -> value))
    this
  }

  def setItem(component: Ii, weight: Double) = {
    loadItems
    items = items.map(_ + (component -> weight))
    this
  }

  override def hashCode() = this.node.map(_.hashCode()).getOrElse(0)
  override def equals(p: Any) = p.isInstanceOf[NeoIi] && node == p.asInstanceOf[NeoIi].node
  override def toString = "Ii(%d)" format id

}
