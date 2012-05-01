package com.owlunit.web.model

import net.liftweb.mongodb.record._
import field.ObjectIdPk
import net.liftweb.record.field.{LongField, StringField}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Movie private extends MongoRecord[Movie] with ObjectIdPk[Movie] {
  def meta = Movie

  object iiid extends LongField(this)
  object name extends StringField(this, 12)

}
object Movie extends Movie with MongoMetaRecord[Movie]


