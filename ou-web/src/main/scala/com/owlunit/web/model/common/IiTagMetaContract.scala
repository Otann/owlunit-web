package com.owlunit.web.model.common

import com.owlunit.core.ii.mutable.Ii
import net.liftweb.common.Box
import org.bson.types.ObjectId

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait IiTagMetaContract[OwnerType <: IiMongoRecord[OwnerType]] {

  def find(id: String): Box[OwnerType]

  def find(id: ObjectId): Box[OwnerType]

  def searchWithName(prefix: String): List[OwnerType]

  protected[model] def loadFromIis(iis: Iterable[Ii]): List[OwnerType]

}
