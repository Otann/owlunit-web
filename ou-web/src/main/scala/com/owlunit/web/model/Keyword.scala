package com.owlunit.web.model

import common.{IiTagMetaContract, IiTagRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.owlunit.core.ii.mutable.Ii
import net.liftweb.util.Helpers._
import net.liftweb.util.FieldContainer
import net.liftweb.mongodb.record.MongoMetaRecord
import org.bson.types.ObjectId
import com.owlunit.web.config.DependencyFactory
import com.owlunit.core.ii.NotFoundException
import net.liftweb.common._
import com.foursquare.rogue.Rogue._
import net.liftweb.mongodb
import net.liftweb.record.field.StringField

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Keyword private() extends IiTagRecord[Keyword] with ObjectIdPk[Keyword] {

  // for MongoRecord
  def meta = Keyword

  // for IiTagRecord and IiTag, init in meta object
  var ii: Ii = null

  override def iiType = "keyword"
  override def iiName = this.name.is.toString

  // Fields
  object name extends StringField(this, "")

  // Field containers
  def createFields = new FieldContainer { def allFields = List(name) }

}

object Keyword extends Keyword with MongoMetaRecord[Keyword] with IiTagMetaContract[Keyword] with Loggable {
  import mongodb.BsonDSL._

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((name.name -> 1)) //TODO(Anton): unique?

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create
    result
  }

  private def loadIi(keyword: Keyword): Box[Keyword] = {
    try {
      keyword.ii = iiDao.load(keyword.informationItemId.is)
      Full(keyword)
    } catch {
      case e: NotFoundException => Failure("Unable to find linked ii", Full(e), Empty)
    }
  }

  // Resolver methods

  override def find(oid: ObjectId) = super.find(oid).flatMap(loadIi)
  override def find(id: String) = if (ObjectId.isValid(id)) find(new ObjectId(id)) else Empty

  def findByName(name: String): Box[Keyword] = {
    val query = Keyword where (_.name eqs name)
    query.fetch() match {
      case Nil => Empty
      case keyword :: Nil => loadIi(keyword)
      case keyword :: _ => {
        logger.error("Multiple keywords found with same name %s" format name)
        loadIi(keyword)
      }
    }
  }

  protected[model] def loadFromIis(iis: Iterable[Ii]) = {
    val iiMap = iis.map(item => (item.id -> item)).toMap
    val query = Keyword where (_.informationItemId in iiMap.keys)

    // Init ii before return
    query.fetch().map(keyword => {
      keyword.ii = iiMap(keyword.informationItemId.is)
      keyword
    })
  }

  def searchWithName(prefix: String) = loadFromIis(prefixSearch(prefix, iiDao))

}