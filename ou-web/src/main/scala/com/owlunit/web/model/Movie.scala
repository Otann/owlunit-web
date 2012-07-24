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

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Movie private extends IiMongoRecord[Movie] with ObjectIdPk[Movie] with IiMovieMeta {

  def meta = Movie
  val baseMeta = "ii.cinema.movie"

  var ii: Ii = null

  // Fields

  object name extends IiStringField(this, ii, Name, 300, "")
  object year extends IntField(this, 0)
  object posterUrl extends StringField(this, "http://placehold.it/130x200")

  object keywords extends MongoListField[Movie, ObjectId](this)
  object persons extends BsonRecordListField(this, CrewItem)

  // Field Containers

  def createFields = new FieldContainer { def allFields = List(name, year, posterUrl) }
  def editFields = new FieldContainer { def allFields = List(name, year, posterUrl) }

  // Manipulation methods

  def addKeyword(k: Keyword, w: Double = KeywordWeight) {
    keywords(k.id.is :: keywords.is)
    ii.setItem(k.ii, w)
  }

  def removeKeyword(k: Keyword) {
    keywords(k.id.is :: keywords.is)
    ii.setItem(k.ii, KeywordWeight)
  }

  def addPerson(p: Person, r: Role.Role) {
    var item = CrewItem.createRecord.person(p.id.is).role(r)
    persons(item :: persons.is) //TODO anton chebotaev - add existence check
    ii.setItem(p.ii, ActorWeight) //TODO anton chebotaev - make it incremental
  }

  override def save = {
    ii.setMeta(SimpleName, simplifyComplexName(name.is, year.is))
    super.save
  }

}

object Movie extends Movie with MongoMetaRecord[Movie] with Loggable {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create.setMeta(Footprint, result.id.toString())
    result
  }

  override def find(oid: ObjectId) = {
    val result = super.find(oid)
    result.map{ m => m.ii = iiDao.load(m.iiid.is)}
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
