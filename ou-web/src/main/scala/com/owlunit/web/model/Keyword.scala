package com.owlunit.web.model

import common.{IiTagContract, IiTagRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.util.Helpers._
import net.liftweb.util.FieldContainer
import net.liftweb.mongodb.record.MongoMetaRecord
import org.bson.types.ObjectId
import com.owlunit.web.config.{IiDaoConfig, DependencyFactory}
import com.owlunit.core.ii.NotFoundException
import net.liftweb.common._
import com.foursquare.rogue.Rogue._
import net.liftweb.mongodb
import net.liftweb.record.field.StringField
import com.mongodb.DBObject

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Keyword private() extends IiTagRecord[Keyword] with ObjectIdPk[Keyword] with Loggable {

  // for MongoRecord
  def meta = Keyword

  // for IiTagRecord
  var ii: Ii = null

  override def kind = "keyword"
  override def name = this.nameField.is

  // Fields
  object nameField extends StringField(this, "")

  // Field containers
  def createFields = new FieldContainer { def allFields = List(nameField) }

}

object Keyword extends Keyword with MongoMetaRecord[Keyword] with IiTagContract[Keyword] with Loggable {
  import mongodb.BsonDSL._

  def iiDao = DependencyFactory.iiDao.vend

  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((nameField.name -> 1)) //TODO(Anton): unique?

  // Creation and Fetching

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

  // Resolver methods

  protected[model] def loadFromIis(iis: Iterable[Ii]) = {
    val iiMap = iis.map(ii => (ii.id -> ii)).toMap
    val query = Keyword where (_.informationItemId in iiMap.keys)
    query.fetch()
  }

  def findByName(name: String): Box[Keyword] = {
    val query = Keyword where (_.nameField eqs name)
    query.fetch() match {
      case Nil => Empty
      case keyword :: Nil => Full(keyword)
      case keyword :: _ => {
        logger.error("Multiple keywords found with same name %s" format name)
        Full(keyword)
      }
    }
  }

  def searchWithName(prefix: String) = loadFromIis(prefixSearch(prefix, iiDao))

}