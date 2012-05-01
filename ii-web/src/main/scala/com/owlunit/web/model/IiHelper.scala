package com.owlunit.web.model

import com.owlunit.core.ii.mutable.Ii
import net.liftweb.common.Box
import net.liftweb.util.Helpers._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiHelper {

  protected def ii: Box[Ii]
  
  def default(key: String): Box[String] = for {
    ii <- ii ?~ "Ii not loaded"
    value <- tryo { ii.loadMeta.meta.get(key) }
  } yield {
    value
  }

  val KeyGlobalSearch = this.getClass.getName + ".SEARCH"

  def simplifyComplexName(args: Any*):String = args.mkString.toLowerCase
    .replaceAll(", the|, a|the |a |", "")
    .replaceAll("[\\W&&\\D]", "")

  def buildQuery(query: String):String = {
    val parts = query.toLowerCase.replaceAll("[^\\w\\d ]", "").split(' ')
    parts.filter(_.length > 2).map(_ + "*").mkString(" ")
  }

}