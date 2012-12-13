package com.owlunit.web.model

import common.{TmdbMetaRecord, TmdbRecord, IiTagMetaRecord, IiTagRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.{LongField, StringField}
import net.liftweb.util.FieldContainer
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.common._
import net.liftweb.mongodb.BsonDSL._
import com.foursquare.rogue.Rogue._
import net.liftweb.mongodb.record.field.DateField
import net.liftweb.util.Helpers._

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */


class Person private() extends TmdbRecord[Person] with ObjectIdPk[Person] {

  // for MongoRecord
  def meta = Person

  // for IiTagRecord
  override var ii: Ii = null
  override def kind = "person"
  override def name = this.fullName.is

  // Fields
  object fullName extends StringField(this, "")
  object photoUrl extends StringField(this, "http://fakeimg.pl/130x200")

  object birthday extends DateField(this)
  object deathday extends DateField(this)

  object bio extends StringField(this, "")

  // Field containers

  def createFields = new FieldContainer { def allFields = List(fullName, photoUrl) }

}

object Person extends Person with TmdbMetaRecord[Person] with Loggable {

  ensureIndex((fullName.name -> 1))

  // Resolver methods

  def findByName(fullName: String): Box[Person] = {
    val query = Person where (_.fullName eqs fullName)
    query.fetch() match {
      case Nil => Empty
      case record :: Nil => loadIi(record)
      case record :: _ => {
        logger.error("Multiple persons found with same name %s" format fullName)
        loadIi(record)
      }
    }
  }

}