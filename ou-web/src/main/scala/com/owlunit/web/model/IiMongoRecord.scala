package com.owlunit.web.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.common.{Box, Empty}
import net.liftweb.record.field.{LongField, StringField}
import net.liftweb.util.Helpers._
import com.owlunit.core.ii.mutable.{IiDao, Ii}
import net.liftweb.mongodb.record.field.{ObjectIdField, UUIDPk}
import net.liftweb.http.js.JsObj


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait IiMongoRecord[OwnerType <: IiMongoRecord[OwnerType]] extends MongoRecord[OwnerType]  {
  self: OwnerType =>

  def ii: Ii

  override def save = {
    ii.save
    informationItemId(ii.id)
    super.save
  }

  protected object informationItemId extends LongField(this)

  protected def simplifyComplexName(args: Any*):String = args.mkString.toLowerCase
    .replaceAll(", the|, a|the |a |", "") // remove articles
    .replaceAll("[\\W&&\\D]", "") // remove all non-chars even spaces

  protected def buildQuery(query: String):String = {
    // spit to words by removing all non-characters and spaces, split with spaces
    val parts = query.toLowerCase.replaceAll("[^\\w\\d ]", "").split(' ')

    // append asterisk to all words longer than 2 characters
    parts.filter(_.length > 2).map(_ + "*").mkString(" ")
  }

}
