package com.owlunit.core.ii.immutable

import org.neo4j.graphdb.Node

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait Ii {

  def id: Long

  def meta: Option[Map[String, String]]
  def metaValue(key: String): Option[String]

  def components: Option[Map[Ii, Double]]
  def componentWeight(component: Ii): Option[Double]

  def parents: Option[Map[Ii, Double]]
  def parentWeight(parent: Ii): Option[Double]

}

private[ii] object Ii {

  private[ii] def apply(node: Node): Ii = new impl.NeoIi(node, None, None, None)

}
