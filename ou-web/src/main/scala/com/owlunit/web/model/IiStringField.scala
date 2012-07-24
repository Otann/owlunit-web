package com.owlunit.web.model

import net.liftweb.record.field.StringField
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.common.Box


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

abstract class IiStringField[OwnerType <: net.liftweb.record.Record[OwnerType]]( rec: OwnerType,
                                                                                 mappedIi: => Ii,
                                                                                 val key: String,
                                                                                 maxLength: Int,
                                                                                 predef: String )
  extends StringField(rec, maxLength, predef) {

  override def apply(in: String) = {
    mappedIi.setMeta(key, in.toLowerCase)
    super.apply(in)
  }

}