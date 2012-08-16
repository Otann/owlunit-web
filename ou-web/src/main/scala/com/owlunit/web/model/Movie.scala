package com.owlunit.web.model

import net.liftweb.record.field._
import net.liftweb.mongodb.{JsonObject,JsonObjectMeta}
import net.liftweb.mongodb.record.{MongoRecord,MongoMetaRecord,MongoId}
import net.liftweb.mongodb.record.field._
import xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.util.{FieldContainer, FieldIdentifier, FieldError}
import net.liftweb.http.{S, SHtml}
import com.foursquare.rogue.Rogue._
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.record.{Field, Record}
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.lib.{BsonRecordMapField, IiMovieMeta}
import com.owlunit.web.lib.IiMovieMeta
import org.bson.types.ObjectId
import net.liftweb.common._
import net.liftweb.mongodb
import net.liftweb.http.js.JsObj
import net.liftweb.http.js.JE.JsObj
import net.liftweb.http.js.JsObj

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Movie private extends IiMongoRecord[Movie] with ObjectIdPk[Movie] with IiMovieMeta with IiTag {

  // for MongoRecord
  def meta = Movie

  // for IiMongoRecord, IiMovieMeta and IiTag
  val baseMeta = "ii.cinema.movie"

  var ii: Ii = null

  def tagId = this.id.is.toString
  def tagCaption = this.name.is.toString

  // Fields

  object name extends IiStringField(this, ii, Name, "")
  object year extends IntField(this, 0)
  object posterUrl extends StringField(this, "http://placehold.it/130x200")

  object keywords extends MongoListField[Movie, ObjectId](this)
  object persons extends BsonRecordListField(this, CrewItem)

  // Data manipulation

  def addKeyword(k: Keyword, w: Double = KeywordWeight) {
    if (!keywords.is.contains(k.id.is)) {
      keywords(k.id.is :: keywords.is)
      ii.setItem(k.ii, w)
    }
  }

  def removeKeyword(k: Keyword) {
    if (keywords.is.contains(k.id.is)) {
      keywords(keywords.is filterNot (_ == k.id.is))
      ii.setItem(k.ii, 0) // TODO(Anton): Check
    }
  }

  def addPerson(p: Person, r: Role.Role) {
    val item = CrewItem.createRecord.person(p.id.is).role(r)
    persons(item :: persons.is)   //TODO(Anton): add existence check
    ii.setItem(p.ii, GeneralPersonWeight) //TODO(Anton): make it incremental
  }

  // Persistence

  override def save = {
    ii.setMeta(SimpleName, simplifyComplexName(name.is, year.is))
    super.save
  }

  // Field Containers

  def createFields = new FieldContainer { def allFields = List(name, year, posterUrl) }
  def editFields =   new FieldContainer { def allFields = List(name, year, posterUrl) }

}

object Movie extends Movie with MongoMetaRecord[Movie] with Loggable {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO(Anton) unsafe vend

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create.setMeta(Footprint, result.id.toString())
    result
  }

  override def find(oid: ObjectId) = {
    val result = super.find(oid)
    result.map{ m => m.ii = iiDao.load(m.informationItemId.is)}
    result
  }

  def findBySimpleName(name: String, year: Int): Box[Movie] = try {
    val simpleName = simplifyComplexName(name, year)
    val ii = iiDao.load(SimpleName, simpleName) match {
      case Nil => return Empty
      case item :: Nil => item
      case item :: _ => {
        logger.error("Multiple movies found with same simplename %s" format simpleName)
        item
      }
    }
    val movieId = ii.loadMeta.meta.get(Footprint)
    find(new ObjectId(movieId))
  } catch {
    case ex: Throwable => Failure("Can't find movie by id (%d)" format id, Full(ex), Empty)
  }

  def findById(in: String): Box[Movie] =
    if (ObjectId.isValid(in))
      find(new ObjectId(in))
    else
      Failure("ObjectIs not valid")

  def findByIiId(id: Long): Box[Movie] = try {
    val movieId = iiDao.load(id).loadMeta.meta.get(Footprint)
    find(new ObjectId(movieId))
  } catch {
    case ex: Throwable => Failure("Can't find movie by id (%d)" format id, Full(ex), Empty)
  }

  def searchByName(prefix: String): Seq[Movie] = {
    //TODO investigate how to fix double load of iis (dao.load + find)
    val ids = iiDao.search(Name, "%s*" format prefix.toLowerCase).map(item => item.loadMeta.meta.get(Footprint))
    val movies = ids.map(id => find(new ObjectId(id)))
    movies.flatten
  }

}
