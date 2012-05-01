package com.owlunit.web.model

import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.{StringField, LongField}
import net.liftweb.util.FieldContainer
import org.bson.types.ObjectId
import net.liftweb.common.{Empty, Box}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Person private () extends MongoRecord[Person] with ObjectIdPk[Person]{
  def meta = Person

  override def toString() = "%s %s" format (firstName.is, lastName.is)
  def url = "/admin/person/" + id

  object firstName extends StringField(this, "") {
    override def displayName = "First name"
    override def validations = valMinLen(1, "Must not be empty") _ :: super.validations
  }
  object lastName extends StringField(this, "") {
    override def displayName = "Last name"
    override def validations = valMinLen(1, "Must not be empty") _ :: super.validations
  }
  object iiid extends LongField(this, 0){ override def displayName = "Id of linked Ii" }

  object photoUrl extends StringField(this, "http://placehold.it/130x200")

  // Fields

  def createFields = new FieldContainer { def allFields = List(firstName, lastName, iiid, photoUrl) }
  
  // Helpers
  
  def fullName = "%s %s" format (firstName.is, lastName.is)

}
object Person extends Person with MongoMetaRecord[Person] {

  def findById(in: String): Box[Person] = if (ObjectId.isValid(in)) find(new ObjectId(in)) else Empty

}