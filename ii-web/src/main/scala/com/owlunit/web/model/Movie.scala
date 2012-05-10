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
import net.liftweb.common.{Failure, Full, Box, Empty}
import org.bson.types.ObjectId
import com.owlunit.web.lib.BsonRecordMapField
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.config.IiMovieMeta

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Movie private extends MongoRecord[Movie] with ObjectIdPk[Movie] with IiModelHelper with IiMovieMeta {
  def meta = Movie

  def url = "/admin/movie/" + id

  object name extends StringField(this, default(Name) openOr "") {
    override def displayName = "Movie name"
    override def validations = valMinLen(1, "Must not be empty") _ :: super.validations
    override def apply(in: String) = {
      ii.map(_.setMeta(Name, in))
      super.apply(in)
    }
  }

  object iiid extends LongField(this, 0) {
    override def displayName = "Movie name"
    override def apply(in: Long) = {
      cachedIi = tryo { meta.iiDao.load(in) }
      super.apply(in)
    }
  }
  
  object persons extends BsonRecordListField[Movie, Role](this, Role) {
    override def displayName = "Linked persons"
    override def apply(in: List[Role]) = {
      // TODO persist roles in Neo
      super.apply(in)
    }
  }
  
  object keywords extends MongoListField(this)
  
  def createFields = new FieldContainer { def allFields = List(name) }
  def editFields = new FieldContainer { def allFields = List(name, iiid) }

  override def toXHtml =
    <span>
      <i class="icon-play-circle"></i>
      <a href={ url }>{ name.is }</a>
    </span>


  private var cachedIi: Box[Ii] = Empty
  def ii = cachedIi
  
  override def save = {
    ii.map(_.save)
    super.save
  }

  def getPersons: Map[Person, Double] = {
    val list = for {
      ii <- ii
      items <- ii.loadItems.items
      (item, weight) <- items
      meta <- item.loadMeta.meta
      id <- meta get Footprint
      person <- Person.findById(id)
    } yield {
      person -> weight
    }
    list.toMap
  }

}

object Movie extends Movie with MongoMetaRecord[Movie] {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  override def createRecord = {
    val result = super.createRecord
    val ii = iiDao.create.setMeta(Footprint, result.id.toString())
    result.iiid(ii.id)
  }


  override def find(oid: ObjectId) = {
    val result = super.find(oid)
    // TODO: load persons
    result
  }

  def findById(in: String): Box[Movie] =
    if (ObjectId.isValid(in))
      find(new ObjectId(in))
    else
      Failure("ObjectIs not valid")
  
  def findByIiId(id: Long): Box[Movie] = try {
    val id = iiDao.load(id).loadMeta.meta.get(Footprint)
    find(new ObjectId(id))
  } catch {
    case ex: Throwable => Failure("Can't find movie by id (%d)" format id, Full(ex), Empty)
  }

}
