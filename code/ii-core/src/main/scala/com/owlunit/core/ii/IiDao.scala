package com.owlunit.core.ii

import impl.NeoIiDao
import collection.mutable.{Map => MutableMap}

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
  private val instances = MutableMap[String, IiDao]()

  def apply(path: String, depth: Int = DefaultDepth): IiDao = synchronized {
    if (!instances.contains(path))
      instances(path) = new NeoIiDao(path, depth)
    instances(path)
  }

}
