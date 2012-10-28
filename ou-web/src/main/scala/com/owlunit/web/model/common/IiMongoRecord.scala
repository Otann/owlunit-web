package com.owlunit.web.model.common

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.record.field.LongField
import com.owlunit.core.ii.mutable.Ii


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Provides communication point between MongoDB and IiDao
 */

trait IiMongoRecord[OwnerType <: IiMongoRecord[OwnerType]] extends MongoRecord[OwnerType] with IiMeta {
  self: OwnerType =>

  // Item has to define and initialize it's own property
  def ii: Ii

  // Field for storing Ii id in MongoDb
  protected object informationItemId extends LongField(this)

  override def save = {
    ii.setMeta(metaObjectId, this.id.toString)
    ii.save

    informationItemId(ii.id)
    super.save
  }

}