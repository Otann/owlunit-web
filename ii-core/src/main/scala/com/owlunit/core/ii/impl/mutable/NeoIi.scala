package com.owlunit.core.ii.impl.mutable

import org.neo4j.graphdb.Node
import com.owlunit.core.ii.Ii

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


private [ii] class NeoIi(dao: NeoIiDao, node: Node) extends Ii {

  def id = node.getId

  def meta = {
    None
  }

  def metaValue(key: String) = Some(node.getProperty(key).asInstanceOf[String])

  def components = null

  def componentWeight(component: Ii) = null

  def parents = null

  def parentWeight(parent: Ii) = null
}