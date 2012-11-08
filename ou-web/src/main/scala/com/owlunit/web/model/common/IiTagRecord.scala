package com.owlunit.web.model.common

import com.owlunit.core.ii.mutable.{Ii, IiDao}
import com.owlunit.web.lib.ui.IiTag
import net.liftweb.common.{Failure, Empty, Full, Box}
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.model.{User, Keyword, Person, Movie}
import org.bson.types.ObjectId
import net.liftweb.record.field.LongField
import net.liftweb.mongodb.record.MongoRecord

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Trait for displayable in UI items
 *         (display functionality moved out to IiTag)
 *         Makes them searchable by indexing caption
 *
 */

trait IiTagRecord[OwnerType <: IiTagRecord[OwnerType]] extends MongoRecord[OwnerType] with IiTag with IiTagMeta {
  self: OwnerType =>

  // Methods required by representation (using mongo ids for now)
  def objectId = this.id.toString

  // Item has to define and initialize it's own property
  def ii: Ii

  // Field for storing Ii id in MongoDb
   object informationItemId extends LongField(this)



  // Methods for save, index and search

  override protected def metaBase = super.metaBase + "." + this.kind
  protected def metaName = metaBase + ".Name"

  override def save = {

    // for global search
    ii.setMeta(metaGlobalId, this.id.toString)
    ii.setMeta(metaGlobalName, name)
    ii.setMeta(metaGlobalType, kind)

    // for local search (only within type)
    ii.setMeta(metaName, name)
    ii.save

    informationItemId(ii.id)
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
    for (ii <- iis) yield IiTag(ii.meta(metaGlobalType), ii.meta(metaGlobalId), ii.meta(metaGlobalName))
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
    iiDao.search(metaGlobalId, id) match {
      case ii :: Nil => Full(IiTag(ii.meta(metaGlobalType), ii.meta(metaGlobalId), ii.meta(metaGlobalName)))
      case Nil => Empty
      case _ => Failure("Multiple found", Empty, Empty)
    }

  }
}