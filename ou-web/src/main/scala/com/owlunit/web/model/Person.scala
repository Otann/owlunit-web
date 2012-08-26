package com.owlunit.web.model

import common.IiMongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.record.field.StringField
import net.liftweb.util.FieldContainer
import org.bson.types.ObjectId
import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory
import net.liftweb.common._
import com.owlunit.web.lib.ui.IiTag
import com.owlunit.web.lib.IiPersonMeta
import net.liftweb.mongodb
import com.owlunit.core.ii.NotFoundException
import net.liftweb.common.Full

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Person private() extends IiMongoRecord[Person] with ObjectIdPk[Person] with IiPersonMeta with IiTag {

  // for MongoRecord
  def meta = Person

  // for IiMongoRecord, IiMovieMeta and IiTag
  val baseMeta = "ii.cinema.person"
  var ii: Ii = null
  def tagId = id.is.toString
  def tagCaption = fullName
  def tagUrl = "#" //TODO(Anton) implement permalinks

  // Fields

  object firstName extends StringField(this, "") {
    override def displayName = "First name"
    override def validations = valMinLen(1, "Must not be empty") _ :: super.validations
  }
  object lastName extends StringField(this, "") {
    override def displayName = "Last name"
    override def validations = valMinLen(1, "Must not be empty") _ :: super.validations
  }

  def fullName = "%s %s" format (firstName.is, lastName.is)

  object photoUrl extends StringField(this, "http://placehold.it/130x200")

  // Field containers
  def createFields = new FieldContainer { def allFields = List(firstName, lastName, photoUrl) }

  override def save = {
    ii.setMeta(Name, fullName) // allow search by full name
    super.save
  }

}

object Person extends Person with MongoMetaRecord[Person] with Loggable {
  import mongodb.BsonDSL._
  import com.foursquare.rogue.Rogue._

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((firstName.name -> 1), unique = true)
  ensureIndex((lastName.name -> 1), unique = true)

  // Creation

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create.setMeta(Footprint, result.id.is.toString)
    result
  }

  // Helper for load methods to init Ii subsystem properly

  private def loadIiForLoaded(record: Person): Box[Person] = {
    try {
      record.ii = iiDao.load(record.informationItemId.is)
      Full(record)
    } catch {
      case e: NotFoundException => Failure("Unable to find linked ii", Full(e), Empty)
    }
  }

  // Resolver methods

  override def find(oid: ObjectId) = super.find(oid).flatMap(loadIiForLoaded)

  def findByName(firstName: String, lastName: String): Box[Person] = {
    val query = Person where (_.firstName eqs firstName) and (_.lastName eqs lastName)
    query.fetch() match {
      case Nil => Empty
      case record :: Nil => loadIiForLoaded(record)
      case keyword :: _ => {
        logger.error("Multiple persons found with same name %s" format fullName)
        loadIiForLoaded(keyword)
      }
    }
  }

  def searchWithName(prefix: String): List[Person] = {
    val iiMap: Map[Long, Ii] = iiDao.search(Name, "%s*" format prefix.toLowerCase).map(item => (item.id -> item)).toMap
    val query = Person where (_.informationItemId in iiMap.keys)
    query.fetch().map(keyword => {
      keyword.ii = iiMap(keyword.informationItemId.is)
      keyword
    })
  }

}