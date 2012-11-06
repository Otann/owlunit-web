package com.owlunit.web.model.common

import com.owlunit.core.ii.mutable.Ii
import net.liftweb.common.Box
import org.bson.types.ObjectId
import net.liftweb.mongodb.record.MongoRecord

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait IiTagContract[OwnerType <: MongoRecord[OwnerType]] {

  def searchWithName(prefix: String): List[OwnerType]

  protected[model] def loadFromIis(iis: Iterable[Ii]): List[OwnerType]

}
