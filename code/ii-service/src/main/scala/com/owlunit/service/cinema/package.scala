package com.owlunit.service

import com.owlunit.core.ii.{IiDao, Ii}


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

package object cinema {

  def withMeta(dao: IiDao, item: Ii):Ii = item.meta match {
    case Some(_) => item
    case None => dao.loadMeta(item)
  }

  def withComponents(dao: IiDao, item: Ii):Ii = item.components match {
    case Some(_) => item
    case None => dao.loadComponents(item)
  }

  def simplifyComplexName(args: Any*):String =
    args.mkString.toLowerCase.replaceAll("[\\W&&\\D]", "")

}

