package com.owlunit.web.model.common

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.LongField
import com.owlunit.core.ii.mutable.{IiDao, Ii}
import net.liftweb.common.{Empty, Failure, Full, Box}
import com.owlunit.core.ii.NotFoundException


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Provides communication point between MongoDB and IiDao
 */

trait IiMongoRecord[OwnerType <: IiMongoRecord[OwnerType]] extends MongoRecord[OwnerType] {
  self: OwnerType =>

  // Item has to define and initialize it's own property
  protected def ii: Ii

  // Base prefix for meta keys and key for storing ObjectId in Neo4j
  protected def baseMeta = "ii.cinema"
  protected def footprintMeta = baseMeta + ".MongoId"

  // Field for storing Ii id in MongoDb
  protected object informationItemId extends LongField(this)

  override def save = {
    // prepare fields
    ii.setMeta(footprintMeta, this.id.toString)
    informationItemId(ii.id)

    // perform save on both sides
    ii.save
    super.save
  }

}