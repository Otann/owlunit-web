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
import com.owlunit.web.config.DependencyFactory
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.record.{Field, Record}
import org.bson.types.ObjectId
import net.liftweb.common.{Failure, Full, Box, Empty}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Movie private extends MongoRecord[Movie] with ObjectIdPk[Movie] with IiHelper {
  def meta = Movie

  val iiFootprint = this.getClass.getName + ".MongoId"
  val iiMetaName  = this.getClass.getName + ".Name"

  protected var ii: Box[Ii] = Empty

  object iiId extends LongField(this)

  object name extends StringField(this, default(iiMetaName) openOr "") {
    override def displayName = "Movie name"
    override def validations = valMinLen(1, "Must not be empty") _ :: super.validations
    override def apply(in: String) = {
      ii.map(_.setMeta(Movie.iiMetaName, in))
      super.apply(in)
    }
  }

  def createFields = new FieldContainer { def allFields = List(name) }
  def editFields = new FieldContainer { def allFields = List(name, iiId) }

  override def toXHtml =
    <span>
      <i class="icon-play-circle"></i>
      <a href={ url }>{ name.is }</a>
    </span>

  def url = "/admin/item/" + id //TODO reuse Site class

  override def save = {
    ii.map(_.save)
    super.save
  }

}

object Movie extends Movie with MongoMetaRecord[Movie] {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  override def createRecord = {
    val result = super.createRecord
    val ii = iiDao.create.setMeta(iiFootprint, result.id.toString())
    result.iiId(ii.id)
  }

  def findById(in: String): Box[Movie] =
    if (ObjectId.isValid(in))
      find(new ObjectId(in))
    else
      Failure("ObjectIs not valid")
  
  def findByNeoId(id: Long): Box[Movie] = try {
    val ii = iiDao.load(id).loadMeta
    val objectIdRaw:String = ii.meta.get(iiFootprint)
    find(new ObjectId(objectIdRaw))
  } catch {
    case ex: Throwable => Failure("Can't find movie by id", Full(ex), Empty)
  }

}


