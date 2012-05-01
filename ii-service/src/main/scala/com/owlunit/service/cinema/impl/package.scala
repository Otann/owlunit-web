package com.owlunit.service.cinema

import com.owlunit.core.ii.{IiDao, Ii}


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

package object impl {

  val KeyGlobalSearch = this.getClass.getName + ".SEARCH"

  def withMeta(dao: IiDao, item: Ii):Ii = item.meta match {
    case Some(_) => item
    case None => dao.loadMeta(item)
  }

  def withComponents(dao: IiDao, item: Ii):Ii = item.components match {
    case Some(_) => item
    case None => dao.loadComponents(item)
  }

  def simplifyComplexName(args: Any*):String = args.mkString.toLowerCase
    .replaceAll(", the|, a|the |a |", "")
    .replaceAll("[\\W&&\\D]", "")

  def buildQuery(query: String):String = {
    val parts = query.toLowerCase.replaceAll("[^\\w\\d ]", "").split(' ')
    parts.filter(_.length > 2).map(_ + "*").mkString(" ")
  }

}

