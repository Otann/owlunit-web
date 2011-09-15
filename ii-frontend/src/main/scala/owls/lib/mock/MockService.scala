package org.owls.lib.mock

import java.lang.Double
import me.prettyprint.hector.api.Keyspace
import java.util.{Collections, UUID, Collection}
import com.manymonkeys.core.ii.{InformationItemDao, InformationItem}
import com.manymonkeys.service.cinema.{MovieService, TagService}

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */


object MockService extends MovieService(null) {

  override def createInformationItem() = null

  override def getByNameSimplified(name: String) = MockItem

  override def createMovie(name: String, year: Long) = MockItem

  override def getAll = MockItem.asCollection

  override def getTag(name: String) = MockItem

  override def createTag(name: String) = MockItem

  override def searchByMetaPrefix(key: String, prefix: String) = {
    val result = new java.util.HashMap[UUID, String]()
    result.put(MockItem.getUUID, MockItem.getMeta(TagService.NAME))
    result
  }

  override def loadByMeta(key: String, value: String) = MockItem.asCollection

  override def removeMeta(item: InformationItem, key: String) {}

  override def setMeta(item: InformationItem, key: String, value: String, isIndexed: Boolean) {}

  override def setMeta(item: InformationItem, key: String, value: String) {}

  override def removeComponent(item: InformationItem, component: InformationItem) {}

  override def setComponentWeight(item: InformationItem, component: InformationItem, weight: Double) {}

  override def loadByUUIDs(uuids: Collection[UUID]) = MockItem.asCollection

  override def loadByUUID(uuid: UUID) = MockItem

  override def reloadParents(items: Collection[InformationItem]) = MockItem.parents.keySet

  override def reloadComponents(items: Collection[InformationItem]) = MockItem.components.keySet

  override def reloadMetadata(items: Collection[InformationItem]) {}

  override def deleteInformationItem(item: InformationItem) {}

}