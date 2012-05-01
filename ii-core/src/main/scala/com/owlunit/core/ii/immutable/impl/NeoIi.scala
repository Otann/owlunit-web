package com.owlunit.core.ii.immutable.impl

import com.owlunit.core.ii.immutable.Ii
import org.neo4j.graphdb.Node

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


private[ii] class NeoIi ( val node: Node,
                          val meta: Option[Map[String, String]],
                          val components: Option[Map[Ii, Double]],
                          val parents: Option[Map[Ii,  Double]]
                          ) extends Ii {

  def id = node.getId
  def metaValue(key: String) = meta.getOrElse(Map.empty).get(key)
  def componentWeight(component: Ii) = components.getOrElse(Map.empty).get(component)
  def parentWeight(parent: Ii) = parents.getOrElse(Map.empty).get(parent)

  override def hashCode() = node.hashCode()
  override def equals(p: Any) = p.isInstanceOf[NeoIi] && node == p.asInstanceOf[NeoIi].node
  override def toString = "Ii(%d)" format id

  def copy( meta: Option[Map[String, String]] = null,
            components: Option[Map[Ii, Double]] = null,
            parents: Option[Map[Ii,  Double]] = null): NeoIi =

    new NeoIi(
      this.node,
      if (meta == null) this.meta else meta,
      if (components == null) this.components else components,
      if (parents == null) this.parents else parents
    )

}

private[ii] object NeoIi {

  implicit def iiToIiImpl(item: Ii): NeoIi =
    if (item.isInstanceOf[NeoIi])
      item.asInstanceOf[NeoIi]
    else
      throw new IllegalArgumentException("This dao can not operate with item " + item.toString)

  def unapply(item: Ii): Option[NeoIi] =
    if (item.isInstanceOf[NeoIi])
      Some(item.asInstanceOf[NeoIi])
    else
      None

}
