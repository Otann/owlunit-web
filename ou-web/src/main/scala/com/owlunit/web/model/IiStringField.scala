package com.owlunit.web.model

import net.liftweb.record.field.StringField
import net.liftweb.record.Record
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.common.Box


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

abstract class IiStringField[OwnerType <: Record[OwnerType]]( parent: OwnerType,
                                                              mappedIi: => Ii,
                                                              val metaKey: String,
                                                              predef: String )
  // 265 limit is ignored by lift-mongo
  extends StringField(parent, 256, predef) {

  override def apply(value: String) = {
    // set value to ii backend
    mappedIi.setMeta(metaKey, value.toLowerCase)

    // sel value to local record
    super.apply(value)
  }

}