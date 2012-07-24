package com.owlunit.web.model

import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.{StringField, LongField}
import net.liftweb.util.FieldContainer
import org.bson.types.ObjectId
import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory
import com.owlunit.core.ii.DAOException
import net.liftweb.common.{Full, Failure, Empty, Box}
import com.owlunit.web.lib.{IiPersonMeta, IiMeta}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Person private () extends IiMongoRecord[Person] with ObjectIdPk[Person] with IiPersonMeta {
  def meta = Person
  val baseMeta = "ii.cinema.person"

  var ii: Ii = null

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

  object photoUrl extends StringField(this, "http://placehold.it/130x200")

  // Fields

  def createFields = new FieldContainer { def allFields = List(firstName, lastName, photoUrl) }

  // Helpers

  def fullName = "%s %s" format (firstName.is, lastName.is)

  // Misc

}
object Person extends Person with MongoMetaRecord[Person] {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create.setMeta(Footprint, result.id.is.toString)
    result
  }

  def findById(in: String): Box[Person] =
    if (ObjectId.isValid(in))
      find(new ObjectId(in))
    else
      Failure("ObjectIs not valid")

  def findById(id: Long): Box[Person] = try {
    val personId = iiDao.load(id).loadMeta.meta.get(Footprint)
    find(new ObjectId(personId))
  } catch {
    case ex: Throwable => Failure("Can't find person by id (%d)" format id, Full(ex), Empty)
  }

}