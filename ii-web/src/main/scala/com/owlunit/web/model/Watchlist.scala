package com.owlunit.web.model

import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.StringField
import net.liftweb.json.JsonDSL._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Watchlist private extends MongoRecord[Watchlist] with ObjectIdPk[Watchlist] {
  def meta = Watchlist

  object name extends StringField(this, 12)

}
object Watchlist extends Watchlist with MongoMetaRecord[Watchlist]