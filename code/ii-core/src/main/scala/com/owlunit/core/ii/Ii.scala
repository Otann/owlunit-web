package com.owlunit.core.ii

import org.neo4j.graphdb.Node

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait Ii {

  def id: Long

  def meta: Option[Map[String, String]]
  def metaValue(key: String) = meta.getOrElse(Map.empty).get(key)

  def components: Option[Map[Ii, Double]]
  def componentWeight(component: Ii) = components.getOrElse(Map.empty).get(component)

  def parents: Option[Map[Ii, Double]]
  def parentWeight(parent: Ii) = parents.getOrElse(Map.empty).get(parent)

}

object Ii {

  private[ii] class IiImpl ( val node: Node,
                             val meta: Option[Map[String, String]],
                             val components: Option[Map[Ii, Double]],
                             val parents: Option[Map[Ii,  Double]]
                             ) extends Ii {

    def id = node.getId

    override def hashCode() = node.hashCode()

    override def equals(p: Any) =
      if (p.isInstanceOf[IiImpl])
        node == p.asInstanceOf[IiImpl].node
      else
        false

    def copy(
              meta: Option[Map[String, String]] = null,
              components: Option[Map[Ii, Double]] = null,
              parents: Option[Map[Ii,  Double]] = null): IiImpl =

      new IiImpl(
        this.node,
        if (meta == null) this.meta else meta,
        if (components == null) this.components else components,
        if (parents == null) this.parents else parents

      )

  }

  private[ii] object IiImpl {

    implicit def iiToIiImpl(item: Ii): IiImpl =
      if (item.isInstanceOf[IiImpl])
        item.asInstanceOf[IiImpl]
      else
        throw new IllegalArgumentException("This dao can not operate with item " + item.toString)

  }

  private[ii] def apply(node: Node): Ii = new IiImpl(node, None, None, None)

}