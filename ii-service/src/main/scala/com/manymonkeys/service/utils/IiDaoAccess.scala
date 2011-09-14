package com.manymonkeys.service.utils

import reflect.BeanProperty
import org.springframework.beans.factory.annotation.Autowired
import com.manymonkeys.core.ii.{Ii, IiDao}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiDaoAccess {

  @BeanProperty
  @Autowired
  var dao: IiDao = null

  def itemWithMeta(item: Ii) =
    if(item.getMetaMap == null) {
      dao.loadMetadata(item)
    } else {
      item
    }

  class NotFoundException(item: String) extends Exception("Requested item %s was not found" format  item)

}