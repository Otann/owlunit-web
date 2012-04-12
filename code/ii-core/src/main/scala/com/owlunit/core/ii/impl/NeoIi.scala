package com.owlunit.core.ii.impl

import com.owlunit.core.ii.Ii
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

  override def hashCode() = node.hashCode()

  override def equals(p: Any) =
    if (p.isInstanceOf[NeoIi])
      node == p.asInstanceOf[NeoIi].node
    else
      false

  def copy(
            meta: Option[Map[String, String]] = null,
            components: Option[Map[Ii, Double]] = null,
            parents: Option[Map[Ii,  Double]] = null): NeoIi =

    new NeoIi(
      this.node,
      if (meta == null) this.meta else meta,
      if (components == null) this.components else components,
      if (parents == null) this.parents else parents

    )

  override def toString = "Ii(%d)" format id
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
