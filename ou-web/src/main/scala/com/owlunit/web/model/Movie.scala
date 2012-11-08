package com.owlunit.web.model

import common.{IiTagContract, IiTagRecord}
import net.liftweb.record.field._
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.util.Helpers._
import net.liftweb.util.FieldContainer
import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory
import net.liftweb.common._
import net.liftweb.mongodb
import com.foursquare.rogue.Rogue._
import com.mongodb.DBObject

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Movie private() extends IiTagRecord[Movie] with ObjectIdPk[Movie] with Loggable {

  // for MongoRecord
  def meta = Movie

  // for IiTagRecord and IiTag, init in meta object
  var ii: Ii = null

  override def kind = "movie"
  override def name = this.nameField.is

  // Fields

  object nameField extends StringField(this, "")
  object yearField extends IntField(this, 0)

  protected object simpleName extends StringField(this, "")
  protected def simplifiedName = simplifyComplexName(nameField.is, yearField.is)

  object posterUrl extends StringField(this, "/static/img/no_poster.png")
  object backdropUrl extends StringField(this, "/static/img/no_backdrop.png")

  object tagline extends StringField(this, "Tagline")

  // Data manipulation

  // helper to define weight values
  private def weight(obj: Any) = obj match {
    case k: Keyword =>     2.0
    case Role.Actor =>     5.0
    case Role.Director => 10.0
    case Role.Producer => 10.0
    case _ =>              1.0
  }

  // keywords are easy, just set weight

  def keywords = Keyword.loadFromIis(ii.items.keys)

  def addKeyword(keyword: Keyword) = {
    this.ii.setItem(keyword.ii, weight(keyword))
    this
  }

  def removeKeyword(keyword: Keyword) = {
    this.ii.setItem(keyword.ii, 0)
    this
  }

  // persons can be attach with multiple roles, store it in mongo

  // List of CrewItem(Role, Person)
  protected object personsObject extends BsonRecordListField(this, CrewItem)

  def allPersons = Person.loadFromIis(ii.items.keys)

  def persons: Map[Role.Role, Seq[Person]] = {
    allPersons.map(p => logger.debug(p.fullName))
    val personsMap = allPersons.map(person => person.id.is -> person).toMap

    val result = for (role <- Role.values) yield role -> personsObject.is.filter(_.role.is == role).map(po => personsMap(po.person.is))
    result.toMap
  }

  def addPerson(person: Person, role: Role.Role) = {
    val item = CrewItem.createRecord.person(person.id.is).role(role)

    // check that if it is not there yet
    if (!personsObject.is.contains(item)) {
      personsObject(item :: personsObject.is)
      val actualWeight = ii.items.getOrElse(person.ii, 0.0)
      ii.setItem(person.ii, actualWeight + weight(role))
    }

    this
  }

  def removePerson(person: Person, role: Role.Role) = {
    val item = CrewItem.createRecord.person(person.id.is).role(role)
    personsObject(personsObject.is.filterNot(_ == item))
    this
  }

  // Persistence

  override def save = {
    simpleName(simplifiedName)
    super.save
  }

  // Field Containers

  def createFields = new FieldContainer { def allFields = List(nameField, yearField, posterUrl) }
  def editFields =   new FieldContainer { def allFields = List(nameField, yearField, posterUrl) }

}

object Movie extends Movie with MongoMetaRecord[Movie] with IiTagContract[Movie] with Loggable {
  import mongodb.BsonDSL._

  def iiDao = DependencyFactory.iiDao.vend

  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((simpleName.name -> 1), unique = true)

  // Creation

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create
    result
  }

  override def fromDBObject(dbo: DBObject) = {
    val result = super.fromDBObject(dbo)
    result.ii = iiDao.load(result.informationItemId.is)
    result
  }

  // Helper for load methods to init Ii subsystem properly

  // Resolver methods

  protected[model] def loadFromIis(iis: Iterable[Ii]) = {
    val iiMap = iis.map(ii => (ii.id -> ii)).toMap
    val query = Movie where (_.informationItemId in iiMap.keys)
    query.fetch().map(record => {
      record.ii = iiMap(record.informationItemId.is)
      record
    })
  }

  def findBySimpleName(name: String, year: Int): Box[Movie] = try {
    val query = Movie where (_.simpleName eqs simplifyComplexName(name, year))
    query.fetch() match {
      case Nil => Empty
      case item :: Nil => Full(item)
      case item :: _ => {
        logger.error("Multiple movies found with same simplename %s" format simpleName)
        Full(item)
      }
    }
  } catch {
    case ex: Throwable => Failure("Can't find movie by id (%s)" format id.is, Full(ex), Empty)
  }



  def searchWithName(prefix: String) = {
    logger.debug("Searching %s prefix in %s" format (prefix, metaName))
    loadFromIis(prefixSearch(prefix, iiDao))
  }

}
