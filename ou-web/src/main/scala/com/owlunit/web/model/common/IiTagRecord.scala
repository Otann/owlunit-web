package com.owlunit.web.model.common

import com.owlunit.core.ii.mutable.IiDao
import com.owlunit.web.lib.ui.IiTag
import net.liftweb.common.{Failure, Empty, Full, Box}
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.model.{User, Keyword, Person, Movie}
import org.bson.types.ObjectId

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Trait for displayable in UI items
 *         (display functionality moved out to IiTag)
 *         Makes them searchable by indexing caption
 *
 */

trait IiTagMeta extends IiMeta {

  protected def metaGlobalName = metaGlobal + ".iiName"
  protected def metaGlobalType = metaGlobal + ".iiType"

}

trait IiTagRecord[OwnerType <: IiTagRecord[OwnerType]] extends IiMongoRecord[OwnerType] with IiTag with IiTagMeta {
  self: OwnerType =>

  // Methods required by representation

  def iiId = this.id.toString

  // Methods for save, index and search

  override def metaBase = super.metaBase + "." + this.iiType
  protected def metaName = metaBase + ".Name"

  override def save = {
    // for local search (only within type)
    ii.setMeta(metaName, iiName)

    // for global search
    ii.setMeta(metaGlobalName, iiName)
    ii.setMeta(metaGlobalType, iiType)
    super.save
  }

  protected def prefixSearch(prefix: String, dao: IiDao) = dao.search(metaName, "%s*" format prefix)

  // Helper methods for descendants

  protected def simplifyComplexName(args: Any*): String = args.mkString.toLowerCase
    .replaceAll(", the|, a|the |a |", "") // remove articles
    .replaceAll("[\\W&&\\D]", "") // remove all non-chars even spaces


}

object IiTagRecord extends IiTagMeta {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO(Anton) unsafe vend

  protected def buildQuery(query: String): String = {
    // spit to words by removing all non-characters and spaces, split with spaces
    val parts = query.toLowerCase.replaceAll("[^\\w\\d ]", "").split(' ')

    // append asterisk to all words longer than 2 characters
    parts.filter(_.length > 2).map(_ + "*").mkString(" ")
  }

  def search(query: String): Iterable[IiTag] = {
    val iis = iiDao.search(metaGlobalName, buildQuery(query))
    for (ii <- iis) yield IiTag(ii.meta(metaGlobalType), ii.meta(metaObjectId), ii.meta(metaGlobalName))
  }

  def load(tag: IiTag): Box[IiTagRecord[_]] = tag match {
    case record: IiTagRecord[_] => Full(record)
    case IiTag("user", id, _) => User.find(id)
    case IiTag("movie", id, _) => Movie.find(id)
    case IiTag("person", id, _) => Person.find(id)
    case IiTag("keyword", id, _) => Keyword.find(id)
    case _ => Empty
  }

  def load(id: String): Box[IiTag] = {
    iiDao.search(metaObjectId, id) match {
      case ii :: Nil => Full(IiTag(ii.meta(metaGlobalType), ii.meta(metaObjectId), ii.meta(metaGlobalName)))
      case Nil => Empty
      case _ => Failure("Multiple found", Empty, Empty)
    }

  }
}