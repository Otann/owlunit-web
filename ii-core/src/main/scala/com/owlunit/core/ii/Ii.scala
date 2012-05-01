package com.owlunit.core.ii

import impl.NeoIi
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

private[ii] object Ii {

  private[ii] def apply(node: Node): Ii = new NeoIi(node, None, None, None)

}
