package com.owlunit.web.model

import net.liftweb.mongodb.record.field.ObjectIdField
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import net.liftweb.record.field.{EnumNameField, StringField}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class CrewItem private () extends BsonRecord[CrewItem] {
  def meta = CrewItem

  object person extends ObjectIdField(this)
  object role extends EnumNameField(this, Role, Role.Undefined)

  def test() {
    role(Role.Actor)
  }
}
object CrewItem extends CrewItem with BsonMetaRecord[CrewItem]

object Role extends Enumeration {
  type Role = Value
  val Undefined, Actor, Director, Produces = Value
}