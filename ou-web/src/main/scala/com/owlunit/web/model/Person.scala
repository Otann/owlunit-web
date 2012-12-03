package com.owlunit.web.model

import common.{IiTagMetaRecord, IiTagRecord}
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
 *         Copyright OwlUnit
 */


class Person private() extends IiTagRecord[Person] with ObjectIdPk[Person] {

  // for MongoRecord
  def meta = Person

  // for IiTagRecord and IiTag, init in meta object
  var ii: Ii = null

  override def kind = "person"
  override def name = fullName

  // Fields

  object firstName extends StringField(this, "")
  object lastName extends StringField(this, "")

  object photoUrl extends StringField(this, "http://fakeimg.pl/130x200")

  def fullName = "%s %s" format (firstName.is, lastName.is)

  // Field containers

  def createFields = new FieldContainer { def allFields = List(firstName, lastName, photoUrl) }

}

object Person extends Person with IiTagMetaRecord[Person] with Loggable {

  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((firstName.name -> 1))
  ensureIndex((lastName.name -> 1))

  // Helper for load methods to init Ii subsystem properly


  // Resolver methods

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

}