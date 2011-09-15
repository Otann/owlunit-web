package com.manymonkeys.research.service.cinema.impl.KeywordService

import com.manymonkeys.research.service.utils.IiDaoAccess
import com.manymonkeys.model.cinema.Keyword
import com.manymonkeys.core.ii.Ii
import KeywordServiceImpl._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait KeywordConverter extends IiDaoAccess {

  implicit def keywordToIi(keyword: Keyword): Ii = {
    val item = dao.load(keyword.uuid)
    if (item == null) {
      throw new NotFoundException(keyword.name)
    } else {
      item
    }
  }

}