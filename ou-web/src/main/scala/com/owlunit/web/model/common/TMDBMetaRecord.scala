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
trait TmdbMetaRecord[OwnerType <: TmdbRecord[OwnerType]] extends IiTagMetaRecord[OwnerType] with Loggable {
  self: OwnerType =>

  ensureIndex((this.tmdbId.name -> 1), unique = true)

  def createTmdbRecord(tmdbId: Long): OwnerType = {
    val result = super.createRecord
    result.tmdbId(tmdbId)
    result
  }

  def findByTMDB(id: Long): Box[OwnerType] = {
    val query = this where (_.tmdbId eqs id)
    query.fetch() match {
      case Nil => Empty
      case tag :: Nil => Full(tag)
      case tag :: _ => {
        logger.error("Multiple tags found with same tmdb-id %s" format id)
        Full(tag)
      }
    }
  }
}
