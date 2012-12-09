package com.owlunit.web.model

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import net.liftweb.mongodb.record.field.{ObjectIdRefField, ObjectIdField}
import net.liftweb.record.field.{LongField, StringField, EnumNameField}
import net.liftweb.common.Full

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         BsonRecord for many-to-many mapping
 *         As many-to-many is anti-pattern for mongo, I see no other way implementing it clearly
 */
class CastItem private () extends BsonRecord[CastItem] {
  def meta = CastItem

  object person extends ObjectIdRefField(this, Person)

  object character extends StringField(this, "")
  object order     extends LongField(this)
  object castId    extends LongField(this)

}
object CastItem extends CastItem with BsonMetaRecord[CastItem]