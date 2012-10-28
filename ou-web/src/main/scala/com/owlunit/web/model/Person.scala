package com.owlunit.web.model

import common.{IiTagMetaContract, IiTagRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.record.field.StringField
import net.liftweb.util.FieldContainer
import org.bson.types.ObjectId
import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory
import net.liftweb.common._
import net.liftweb.mongodb
import com.owlunit.core.ii.NotFoundException
import mongodb.BsonDSL._
import com.foursquare.rogue.Rogue._
import net.liftweb.util.Helpers._
import net.liftweb.common.Full

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Person private() extends IiTagRecord[Person] with ObjectIdPk[Person] {

  // for MongoRecord
  def meta = Person

  // for IiTagRecord and IiTag, init in meta object
  var ii: Ii = null

  override def iiType = "person"
  override def iiName = fullName

  // Fields

  object firstName extends StringField(this, "")
  object lastName extends StringField(this, "")

  object photoUrl extends StringField(this, "http://placehold.it/130x200")

  def fullName = "%s %s" format (firstName.is, lastName.is)

  // Field containers

  def createFields = new FieldContainer { def allFields = List(firstName, lastName, photoUrl) }

}

object Person extends Person with MongoMetaRecord[Person] with IiTagMetaContract[Person] with Loggable {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((firstName.name -> 1))
  ensureIndex((lastName.name -> 1))

  // Creation

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create
    result
  }

  // Helper for load methods to init Ii subsystem properly

  private def loadIi(record: Person): Box[Person] = {
    try {
      record.ii = iiDao.load(record.informationItemId.is)
      Full(record)
    } catch {
      case e: NotFoundException => Failure("Unable to find linked ii", Full(e), Empty)
    }
  }

  // Resolver methods

  override def find(oid: ObjectId) = super.find(oid).flatMap(loadIi)
  override def find(id: String) = if (ObjectId.isValid(id)) find(new ObjectId(id)) else Empty

  def findByName(firstName: String, lastName: String): Box[Person] = {
    val query = Person where (_.firstName eqs firstName) and (_.lastName eqs lastName)
    query.fetch() match {
      case Nil => Empty
      case record :: Nil => loadIi(record)
      case keyword :: _ => {
        logger.error("Multiple persons found with same name %s" format fullName)
        loadIi(keyword)
      }
    }
  }

  protected[model] def loadFromIis(iis: Iterable[Ii]) = {
    val iiMap = iis.map(item => (item.id -> item)).toMap
    val query = Person where (_.informationItemId in iiMap.keys)

    query.fetch().map(keyword => {
      keyword.ii = iiMap(keyword.informationItemId.is)
      keyword
    })
  }

  def searchWithName(prefix: String) = loadFromIis(prefixSearch(prefix, iiDao))
}