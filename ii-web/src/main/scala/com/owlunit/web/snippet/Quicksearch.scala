//package com.owlunit.web.snippet
//
//import net.liftweb.util._
//import net.liftweb.common._
//import java.util.Date
//import com.owlunit.web.lib._
//import Helpers._
//import com.owlunit.web.lib.CinemaUtilities._
//import net.liftweb.http.js.JsCmd
//import net.liftweb.http.js.JsCmds._
//import net.liftweb.http.js.JE.JsRaw
//import scala.xml.NodeSeq.seqToNodeSeq
//import net.liftweb.http.js.JsCmds
//import net.liftweb.http.SHtml
//import net.liftweb.util.Helpers.nextFuncName
//import net.liftweb.http.{S, SHtml}
//import collection.mutable.ListBuffer
//import com.owlunit.service.cinema._
//import stub._
//import xml.{Null, Attribute, NodeSeq, Text}
//import com.owlunit.web.config.DependencyFactory
//
///**
// * @author Anton Chebotaev
// *         Owls Proprietary
// */
//class Quicksearch {
//
//  //TODO: drag n drop - http://comments.gmane.org/gmane.comp.web.lift/32712
//
//  // Fallback - use DependencyFactory.cinemaService.make
////  lazy val cinemaService = DependencyFactory.inject[CinemaService] openOr CinemaServiceStub
//  lazy val cinemaService = DependencyFactory.cinemaService.make openOr CinemaServiceStub
//  lazy val log = Logger(this.getClass)
//
//  val minChars = 3
//  val resultsId = nextFuncName
//
//  def onChange = SHtml.onEvent(prefix => {
//    if (prefix.length >= 3) {
//      JsCmds.SetHtml(resultsId, loadItems(prefix)) & JsCmds.JsShowId(resultsId)
//    } else {
//      JsCmds.SetHtml(resultsId, NodeSeq.Empty) & JsCmds.JsHideId(resultsId)
//    }
//  })
//
//  private def loadItems(prefix: String) = {
//    val opts = cinemaService.search(prefix)
//    opts.map(k => {
//      val icon = k match {
//        case x: KeywordIi => "icon-tag"
//        case x: PersonIi => "icon-user"
//        case x: MovieIi => "icon-facetime-video"
//      }
//      <li><a href={"/admin/item/" + k.id}><i class={icon}></i> {k.title}</a></li>
//    })
//  }
//
//  def render = {
//      "name=search [oninput]" #> onChange &
//      ".dropdown-menu [id]" #> resultsId &
//      "form [class]" #> "navbar-search pull-right"
//  }
//
//}
