package com.owlunit.web.model.common

import net.liftweb.util.Helpers._
import com.owlunit.core.ii.mutable.IiDao
import com.owlunit.web.lib.ui.IiTag

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Trait for displayable in UI items
 *         (display functionality moved out to IiTag)
 *         Makes them searchable by indexing caption
 *
 */

trait IiTagRecord[OwnerType <: IiTagRecord[OwnerType]] extends IiMongoRecord[OwnerType] with IiTag {
  self: OwnerType =>

  // Methods required by representation

  def tagId = this.id.toString

  // Methods for save, index and search

  override def baseMeta = super.baseMeta + "." + this.tagType
  protected def captionMeta = baseMeta + ".Name"

  override def save = {
    ii.setMeta(captionMeta, tagCaption)
    super.save
  }

  protected def searchIi(prefix: String, dao: IiDao) = dao.search(captionMeta, "%s*" format prefix.toLowerCase)

  // Helper methods for descendants

  protected def simplifyComplexName(args: Any*): String = args.mkString.toLowerCase
    .replaceAll(", the|, a|the |a |", "") // remove articles
    .replaceAll("[\\W&&\\D]", "") // remove all non-chars even spaces

  protected def buildQuery(query: String): String = {
    // spit to words by removing all non-characters and spaces, split with spaces
    val parts = query.toLowerCase.replaceAll("[^\\w\\d ]", "").split(' ')

    // append asterisk to all words longer than 2 characters
    parts.filter(_.length > 2).map(_ + "*").mkString(" ")
  }

}
