package com.owlunit.web.model

import net.liftweb.mongodb.record.field.ObjectIdPk
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.common.{Empty, Box}
import net.liftweb.record.field.{StringField, LongField}
import net.liftweb.util.Helpers._
import net.liftweb.util.FieldContainer
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import org.bson.types.ObjectId
import com.owlunit.web.config.{IiMeta, DependencyFactory}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Keyword private () extends MongoRecord[Keyword] with ObjectIdPk[Keyword] with IiModelHelper with IiMeta {
  def meta = Keyword

  override def toString() = name.is

  object name extends StringField(this, "") {
    override def displayName = "Keyword name"
    override def validations = valMinLen(1, "Must not be empty") _ :: super.validations
  }

  // Fields

  def createFields = new FieldContainer { def allFields = List(name) }

  // Misc

  override def save = {
    ii.map(_.save)
    super.save
  }

  private var cachedIi: Box[Ii] = Empty
  def ii = cachedIi
  
  object iiid extends LongField(this) {
    override def displayName = "Movie name"
    override def apply(in: Long) = {
      cachedIi = tryo { meta.iiDao.load(in) }
      super.apply(in)
    }
  }

}
object Keyword extends Keyword with MongoMetaRecord[Keyword] {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend


  override def find(oid: ObjectId) = {
    super.find(oid)
  }

  override def createRecord = {
    val result = super.createRecord
    val ii = iiDao.create.setMeta(Footprint, result.id.toString())
    result.iiid(ii.id)
  }

  def findById(in: String): Box[Keyword] = if (ObjectId.isValid(in)) find(new ObjectId(in)) else Empty

}