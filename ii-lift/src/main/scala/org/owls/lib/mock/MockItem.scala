package org.owls.lib.mock

import com.manymonkeys.core.ii.InformationItem
import com.manymonkeys.service.cinema.TagService
import java.util.{Collections, Collection, HashMap, UUID}

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */

class MockItem(name : String) extends InformationItem {

  val meta = new java.util.HashMap[String, String]()
  meta.put(TagService.NAME, name)

  val parents = new java.util.HashMap[InformationItem, java.lang.Double]()
  val components = new java.util.HashMap[InformationItem, java.lang.Double]()

  def getParentWeight(parent: InformationItem) = parents.get(parent)

  def getParents = parents

  def getComponentWeight(component: InformationItem) = components.get(component)

  def getComponents = components

  def getMeta(key: String) = meta.get(key)

  def getMetaMap = meta

  def getUUID = UUID.randomUUID()

}

object MockItem extends MockItem("Item Name") {
  components.put(new MockItem("First Component"), 10)
  components.put(new MockItem("Another One"), 7)
  components.put(new MockItem("single"), 7)

  val asCollection : Collection[InformationItem] = Collections.singleton(MockItem)
}
