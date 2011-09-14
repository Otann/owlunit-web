package com.manymonkeys.service.cinema.impl.KeywordService

import com.manymonkeys.service.utils.IiDaoAccess
import com.manymonkeys.model.cinema.Keyword
import com.manymonkeys.core.ii.Ii
import KeywordServiceImpl._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait KeywordConverter extends IiDaoAccess {

  implicit def keywordToIi(keyword: Keyword): Ii = {
    if (keyword.uuid != null) {
      val item = dao.load(keyword.uuid)
      if (item == null) {
        throw new NotFoundException(keyword.name)
      } else {
        item
      }
    } else {
      val items = dao.load(KEY_NAME, keyword.name)
      if (items.isEmpty) {
        throw new NotFoundException(keyword.name)
      } else {
        items.iterator().next()
      }
    }
  }

}