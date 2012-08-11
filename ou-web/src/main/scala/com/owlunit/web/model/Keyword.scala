package com.owlunit.web.model

import net.liftweb.mongodb.record.field.ObjectIdPk
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.record.field.{StringField, LongField}
import net.liftweb.util.Helpers._
import net.liftweb.util.FieldContainer
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import org.bson.types.ObjectId
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.lib.{IiMeta}
import com.owlunit.core.ii.NotFoundException
import net.liftweb.common._
import net.liftweb.http.js.JE.JsObj

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Keyword private () extends IiMongoRecord[Keyword] with ObjectIdPk[Keyword] with IiMeta with IiTag {
  def meta = Keyword
  val baseMeta = "ii.cinema.keyword"

  var ii: Ii = null

  def tagId = this.id.is.toString
  def tagCaption = this.name.is.toString

  object name extends IiStringField(this, ii, Name, 140, "")

  // Field groups
  def createFields = new FieldContainer { def allFields = List(name) }

}


object Keyword extends Keyword with MongoMetaRecord[Keyword] with Loggable {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create.setMeta(Footprint, result.id.toString())
    result
  }
  
  override def find(oid: ObjectId) = {
    try {
      for {result <- super.find(oid)} yield {
        result.ii = iiDao.load(iiid.is)
        result
      }
    } catch {
      case e: NotFoundException => Failure("Unable to find linked ii", Full(e), Empty)
    }
  }

  def createFromIi(ii: Ii) = {
    val oid = new ObjectId(ii.loadMeta.meta.get(Footprint))
    find(oid)
  }

  def findByName(name: String): Box[Keyword] = {
    //TODO investigate how to fix double load of iis (dao.load + find)
    val ids = iiDao.load(Name, name).map(item => item.loadMeta.meta.get(Footprint))
    val keywords = ids.map(id => find(new ObjectId(id))).flatten
    keywords match {
      case Nil => Empty
      case keyword :: Nil => Full(keyword)
      case keyword :: _ => {
        logger.error("Multiple keywords found with same name %s" format name)
        Full(keyword)
      }
    }
  }

  def searchByName(prefix: String): Seq[Keyword] = {
    //TODO investigate how to fix double load of iis (dao.load + find)
    val ids = iiDao.search(Name, "%s*" format prefix.toLowerCase).map(item => item.loadMeta.meta.get(Footprint))
    val keywords = ids.map(id => find(new ObjectId(id)))
    keywords.flatten
  }

}