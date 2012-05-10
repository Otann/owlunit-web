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

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Person private () extends MongoRecord[Person] with ObjectIdPk[Person] with IiModelHelper {
  def meta = Person

  override def toString() = "%s %s" format (firstName.is, lastName.is)
  def url = "/admin/person/" + id

  val iiFootprint = this.getClass.getName + ".MongoId"
  protected var ii: Box[Ii] = Empty
  object iiid extends LongField(this)

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

  def createFields = new FieldContainer { def allFields = List(firstName, lastName, iiid, photoUrl) }
  
  // Helpers
  
  def fullName = "%s %s" format (firstName.is, lastName.is)

  // Misc

  override def save = {
    ii.map(_.save)
    super.save
  }
}
object Person extends Person with MongoMetaRecord[Person] {

  def findById(in: String): Box[Person] = if (ObjectId.isValid(in)) find(new ObjectId(in)) else Empty

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  override def createRecord = {
    val result = super.createRecord
    val ii = iiDao.create.setMeta(iiFootprint, result.id.toString())
    result.iiid(ii.id)
  }

}