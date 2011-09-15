package com.manymonkeys.research.service.cinema.impl.KeywordService

import com.manymonkeys.service.cinema.KeywordService
import com.manymonkeys.model.cinema.Keyword
import java.util.UUID
import com.manymonkeys.research.service.utils.IiDaoAccess
import com.manymonkeys.core.ii.Ii
import scalaj.collection.Imports._
import KeywordServiceImpl._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
object KeywordServiceImpl {

  final val CREATOR_KEY   = classOf[KeywordService].getName
  final val CREATOR_VALUE = "#"
  final val KEY_NAME      = classOf[KeywordService].getName + ".NAME"

}

class KeywordServiceImpl extends KeywordService
                         with IiDaoAccess
                         with KeywordConverter {

  implicit def iiToKeyword(item: Ii): Keyword = {
    val meta = itemWithMeta(item)
    Keyword(uuid = meta.getUUID, name = meta.getMeta(KEY_NAME))
  }

  def createKeyword(name: String) = {
    val item = dao.createInformationItem()
    dao.setMeta(item, KEY_NAME, name)
    dao.setUnindexedMeta(item, CREATOR_KEY, CREATOR_VALUE)
  }

  def loadKeyword(uuid: UUID) = dao.load(uuid)

  def loadKeyword(name: String) = {
    val items = dao.load(KEY_NAME, name)
    if (items.isEmpty) {
      throw new NotFoundException(name)
    } else {
      items.iterator().next()
    }
  }

  def listKeywords = dao.load(CREATOR_KEY, CREATOR_VALUE).asScala.map(iiToKeyword(_)).toSeq

  def updateName(keyword: Keyword, name: String) = dao.setMeta(keyword, KEY_NAME, name)

  def isKeyword(keyword: Keyword) = itemWithMeta(keyword).getMeta(CREATOR_KEY) != null

}