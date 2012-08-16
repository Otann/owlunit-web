package com.owlunit.web.model

import net.liftweb.mongodb.record.field.ObjectIdField
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import net.liftweb.record.field.EnumNameField

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         BsonRecord fot many-to-many mapping
 *         As many-to-many is anti-pattern for mongo, I see no other way implementing it clearly
 */

class CrewItem private () extends BsonRecord[CrewItem] {
  def meta = CrewItem

  object person extends ObjectIdField(this)
  object role extends EnumNameField(this, Role, Role.Undefined)

}
object CrewItem extends CrewItem with BsonMetaRecord[CrewItem]

object Role extends Enumeration {
  type Role = Value
  val Undefined, Actor, Director, Produces = Value
}