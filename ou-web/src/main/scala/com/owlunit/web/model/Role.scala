package com.owlunit.web.model

import net.liftweb.mongodb.record.field.ObjectIdField
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import net.liftweb.record.field.{EnumNameField, StringField}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Role private () extends BsonRecord[Role] {
  def meta = Role

  object person extends ObjectIdField(this)
  object role extends EnumNameField(this, RoleType.Undefined)


}
object Role extends Role with BsonMetaRecord[Role]

object RoleType extends Enumeration {
  type RoleType = Value
  val Undefined = Value

  val Actor = Value
  val Director = Value
  val Producer = Value
}