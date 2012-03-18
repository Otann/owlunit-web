package com.owlunit.core.ii

import scala.collection.mutable.{Map => MutableMap}
import scala.collection.JavaConversions._

import org.neo4j.graphdb.Node

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait Ii {

  def id: Long

  def meta: Map[String, String]
  def metaValue(key: String) = meta(key)

  def components: Option[Map[Ii, Double]]
  def componentWeight(component: Ii) = components.getOrElse(Map.empty)(component)

  def parents: Option[Map[Ii, Double]]
  def parentWeight(parent: Ii) = parents.getOrElse(Map.empty)(parent)

}

object Ii {

  private[ii] case class IiImpl (
    node: Node,
    meta: Map[String, String],
    components: Option[Map[Ii, Double]],
    parents: Option[Map[Ii,  Double]]
  ) extends Ii {

    def id = node.getId

  }

  private[ii] object IiImpl {

    implicit def iiToIiImpl(item: Ii): IiImpl =
      if (item.isInstanceOf[IiImpl])
        item.asInstanceOf[IiImpl]
      else
        throw new IllegalArgumentException("This dao can not operate with item " + item.toString)

  }


  private[ii] def apply(node: Node): Ii = {

    val meta = MutableMap[String, String]()
    for (key <- node.getPropertyKeys.toSeq) {
      meta.put(key, node.getProperty(key).toString)
    }

    new IiImpl(node, meta.toMap, None, None)
  }

}