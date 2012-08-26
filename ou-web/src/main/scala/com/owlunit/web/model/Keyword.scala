package com.owlunit.web.model

import common.{IiStringField, IiMongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.util.Helpers._
import net.liftweb.util.FieldContainer
import net.liftweb.mongodb.record.MongoMetaRecord
import org.bson.types.ObjectId
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.lib.ui.IiTag
import com.owlunit.web.lib.IiMeta
import com.owlunit.core.ii.NotFoundException
import net.liftweb.common._
import com.foursquare.rogue.Rogue._
import net.liftweb.mongodb

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Keyword private() extends IiMongoRecord[Keyword] with ObjectIdPk[Keyword] with IiMeta with IiTag {

  // for MongoRecord
  def meta = Keyword

  // for IiMongoRecord, IiMovieMeta and IiTag
  val baseMeta = "ii.cinema.keyword"
  var ii: Ii = null
  override def tagId = this.id.is.toString
  override def tagCaption = this.name.is.toString
  def tagUrl = "#" //TODO(Anton) implement permalinks

  // Fields
  object name extends IiStringField(this, ii, Name, "")

  // Field containers
  def createFields = new FieldContainer { def allFields = List(name) }

}

object Keyword extends Keyword with MongoMetaRecord[Keyword] with Loggable {
  import mongodb.BsonDSL._

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  ensureIndex((informationItemId.name -> 1), unique = true)

  // Creation

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create.setMeta(Footprint, result.id.toString())
    result
  }

  // Helper for load methods to init Ii subsystem properly

  private def loadIiForLoaded(keyword: Keyword): Box[Keyword] = {
    try {
      keyword.ii = iiDao.load(keyword.informationItemId.is)
      Full(keyword)
    } catch {
      case e: NotFoundException => Failure("Unable to find linked ii", Full(e), Empty)
    }
  }

  // Resolver methods

  override def find(oid: ObjectId) = super.find(oid).flatMap(loadIiForLoaded)

  def findByName(name: String): Box[Keyword] = {
    val query = Keyword where (_.name eqs name)
    query.fetch() match {
      case Nil => Empty
      case keyword :: Nil => loadIiForLoaded(keyword)
      case keyword :: _ => {
        logger.error("Multiple keywords found with same name %s" format name)
        loadIiForLoaded(keyword)
      }
    }
  }

  def searchWithName(prefix: String): List[Keyword] = {
    val iiMap: Map[Long, Ii] = iiDao.search(Name, "%s*" format prefix.toLowerCase).map(item => (item.id -> item)).toMap
    val query = Keyword where (_.informationItemId in iiMap.keys)
    query.fetch().map(keyword => {
      keyword.ii = iiMap(keyword.informationItemId.is)
      keyword
    })
  }

}