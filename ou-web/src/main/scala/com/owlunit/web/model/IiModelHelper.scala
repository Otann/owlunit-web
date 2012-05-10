package com.owlunit.web.model

import net.liftweb.util.Helpers._
import net.liftweb.record.field.LongField
import com.owlunit.core.ii.mutable.{IiDao, Ii}
import net.liftweb.common.{Full, Empty, Box}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiModelHelper {

//  def dao: IiDao
  def ii: Box[Ii]
  
  def default(key: String): Box[String] = for {
    ii <- ii ?~ "Ii not loaded"
    value <- tryo { ii.loadMeta.meta.get(key) }
  } yield {
    value
  }

  def simplifyComplexName(args: Any*):String = args.mkString.toLowerCase
    .replaceAll(", the|, a|the |a |", "")
    .replaceAll("[\\W&&\\D]", "")

  def buildQuery(query: String):String = {
    val parts = query.toLowerCase.replaceAll("[^\\w\\d ]", "").split(' ')
    parts.filter(_.length > 2).map(_ + "*").mkString(" ")
  }
  

}