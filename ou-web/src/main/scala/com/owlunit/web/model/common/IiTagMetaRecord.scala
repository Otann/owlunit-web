package com.owlunit.web.model.common

import com.owlunit.core.ii.mutable.{IiDao, Ii}
import net.liftweb.common._
import org.bson.types.ObjectId
import net.liftweb.mongodb.record.{BsonRecord, BsonMetaRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.owlunit.web.lib.ui.IiTag
import net.liftweb.mongodb.MongoMeta
import com.owlunit.web.config.DependencyFactory
import com.mongodb.DBObject
import net.liftweb.mongodb
import net.liftweb.mongodb.BsonDSL._
import com.foursquare.rogue.Rogue._
import com.owlunit.core.ii.NotFoundException
import net.liftweb.common.Full

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
trait IiTagMetaRecord[OwnerType <: IiTagRecord[OwnerType]] extends MongoMetaRecord[OwnerType] with Loggable {
  self: OwnerType =>

  ensureIndex((this.informationItemId.name -> 1), unique = true)

  def iiDao = DependencyFactory.iiDao.vend

  override def createRecord: OwnerType = {
    val result = super.createRecord
    result.ii = iiDao.create
    result
  }

  override def fromDBObject(dbo: DBObject) = {
    val result = super.fromDBObject(dbo)
    result.ii = iiDao.load(result.informationItemId.is)
    result
  }

  def searchWithName(prefix: String) = loadFromIis(prefixSearch(prefix))

  protected[model] def loadFromIis(iis: Iterable[Ii]) = {
    val iiMap = iis.map(ii => (ii.id -> ii)).toMap
    val query = this where (_.informationItemId in iiMap.keys)
    query.fetch()
  }

  protected[model] def loadIi(record: OwnerType): Box[OwnerType] = {
    try {
      record.ii = iiDao.load(record.informationItemId.is)
      Full(record)
    } catch {
      case e: NotFoundException => Failure("Unable to find linked ii", Full(e), Empty)
    }
  }

  protected[model] def prefixSearch(prefix: String) = iiDao.search(this.metaName, "%s*" format prefix)

}
