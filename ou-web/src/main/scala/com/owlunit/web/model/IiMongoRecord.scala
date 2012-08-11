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
//  def toJSON: JsObj

  override def save = {
    ii.save
    iiid(ii.id)
    super.save
  }

  protected object iiid extends LongField(this)

  protected def simplifyComplexName(args: Any*):String = args.mkString.toLowerCase
    .replaceAll(", the|, a|the |a |", "")
    .replaceAll("[\\W&&\\D]", "")

  protected def buildQuery(query: String):String = {
    val parts = query.toLowerCase.replaceAll("[^\\w\\d ]", "").split(' ')
    parts.filter(_.length > 2).map(_ + "*").mkString(" ")
  }

}
