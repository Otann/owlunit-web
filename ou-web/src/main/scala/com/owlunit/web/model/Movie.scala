package com.owlunit.web.model

import common.{IiStringField, IiMongoRecord}
import net.liftweb.record.field._
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.util.Helpers._
import net.liftweb.util.FieldContainer
import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.lib.{IiTag, IiMovieMeta}
import org.bson.types.ObjectId
import net.liftweb.common._
import net.liftweb.mongodb
import com.owlunit.core.ii.NotFoundException
import com.foursquare.rogue.Rogue._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Movie private() extends IiMongoRecord[Movie] with ObjectIdPk[Movie] with IiMovieMeta with IiTag {

  // for MongoRecord
  def meta = Movie

  // for IiMongoRecord, IiMovieMeta and IiTag

  val baseMeta = "ii.cinema.movie"
  var ii: Ii = null
  def tagId = this.id.is.toString
  def tagCaption = this.name.is.toString
  def tagUrl = "#" //TODO(Anton) implement permalinks

  // Fields

  object name extends IiStringField(this, ii, Name, "")
  object year extends IntField(this, 0)
  protected object simpleName extends StringField(this, "")

  object posterUrl extends StringField(this, "http://placehold.it/130x200")

  object keywords extends MongoListField[Movie, ObjectId](this)
  object persons extends BsonRecordListField(this, CrewItem)

  // Data manipulation

  def addKeyword(k: Keyword, w: Double = KeywordWeight) = {
    if (!keywords.is.contains(k.id.is)) {
      keywords(k.id.is :: keywords.is)
      ii.setItem(k.ii, w)
    }
    this
  }

  def removeKeyword(k: Keyword) = {
    if (keywords.is.contains(k.id.is)) {
      keywords(keywords.is filterNot (_ == k.id.is))
      ii.removeItem(k.ii)
    }
    this
  }

  def addPerson(person: Person, role: Role.Role) = {
    val item = CrewItem.createRecord.person(person.id.is).role(role)

    // check that is not there yet
    if (!persons.is.contains(item)) {
      persons(item :: persons.is)
      val weight = ii.loadItems.items.get.getOrElse(person.ii, 0.0)

      role match {
        case Role.Actor    => ii.setItem(person.ii, weight + ActorWeight)
        case Role.Director => ii.setItem(person.ii, weight + DirectorWeight)
        case Role.Producer => ii.setItem(person.ii, weight + ProducerWeight)
        case _ => ii.setItem(person.ii, weight + GeneralPersonWeight)
      }
    }

    this
  }

  // Persistence

  protected def simplifyName = simplifyComplexName(name.is, year.is)

  override def save = {
    simpleName(simplifyName)
    super.save
  }

  // Field Containers

  def createFields = new FieldContainer { def allFields = List(name, year, posterUrl) }
  def editFields =   new FieldContainer { def allFields = List(name, year, posterUrl) }

}

object Movie extends Movie with MongoMetaRecord[Movie] with Loggable {
  import mongodb.BsonDSL._

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO(Anton) unsafe vend

  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((simpleName.name -> 1), unique = true)

  // Creation

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create.setMeta(Footprint, result.id.toString())
    result
  }

  // Helper for load methods to init Ii subsystem properly

  private def loadIiForLoaded(record: Movie): Box[Movie] = {
    try {
      record.ii = iiDao.load(record.informationItemId.is)
      Full(record)
    } catch {
      case e: NotFoundException => Failure("Unable to find linked ii", Full(e), Empty)
    }
  }

  // Resolver methods

  override def find(oid: ObjectId) = super.find(oid).flatMap(loadIiForLoaded)

  def findBySimpleName(name: String, year: Int): Box[Movie] = try {
    val query = Movie where (_.simpleName eqs simplifyComplexName(name, year))
    query.fetch() match {
      case Nil => Empty
      case item :: Nil => loadIiForLoaded(item)
      case item :: _ => {
        logger.error("Multiple movies found with same simplename %s" format simpleName)
        loadIiForLoaded(item)
      }
    }
  } catch {
    case ex: Throwable => Failure("Can't find movie by id (%s)" format id.is, Full(ex), Empty)
  }

  def searchWithName(prefix: String): List[Movie] = {
    val iiMap: Map[Long, Ii] = iiDao.search(Name, "%s*" format prefix.toLowerCase).map(item => (item.id -> item)).toMap
    val query = Movie where (_.informationItemId in iiMap.keys)
    query.fetch().map(record => {
      record.ii = iiMap(record.informationItemId.is)
      record
    })
  }

}
