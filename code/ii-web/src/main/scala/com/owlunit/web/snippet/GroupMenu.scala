package com.owlunit.web.snippet

import com.owlunit.web.lib.AppHelpers

import scala.xml.NodeSeq

import net.liftweb._
import http.{LiftRules, S}
import sitemap.SiteMap

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object GroupMenu extends AppHelpers {
  def render(in: NodeSeq): NodeSeq = {
    for {
      group <- S.attr("group") ?~ "Group not specified"
      sitemap <- LiftRules.siteMap ?~ "Sitemap is empty"
      request <- S.request ?~ "Request is empty"
      curLoc <- request.location ?~ "Current location is empty"
    } yield {
      val currentClass = S.attr("current_class").openOr("current")
      sitemap.locForGroup(group) flatMap { loc =>
        if (curLoc.name == loc.name)
          <li class={currentClass}>{SiteMap.buildLink(loc.name)}</li>
        else
          <li>{SiteMap.buildLink(loc.name)}</li>
      }
    }: NodeSeq
  }
}
