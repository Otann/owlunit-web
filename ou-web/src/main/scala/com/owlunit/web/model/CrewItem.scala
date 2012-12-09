package com.owlunit.web.model

import net.liftweb.mongodb.record.field.{ObjectIdRefField, ObjectIdField}
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import net.liftweb.record.field.{StringField, EnumNameField}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         BsonRecord for many-to-many mapping
 *         As many-to-many is anti-pattern for mongo, I see no other way implementing it clearly
 */
class CrewItem private () extends BsonRecord[CrewItem] {
  def meta = CrewItem

  object person extends ObjectIdRefField[CrewItem, Person](this, Person)

  object job        extends StringField(this, "")
  object department extends StringField(this, "")

}
object CrewItem extends CrewItem with BsonMetaRecord[CrewItem]