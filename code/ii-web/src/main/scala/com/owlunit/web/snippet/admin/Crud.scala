package com.owlunit.web.snippet.admin

import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import com.owlunit.web.lib._
import Helpers._
import com.owlunit.web.lib.CinemaUtilities._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import scala.xml.NodeSeq.seqToNodeSeq
import net.liftweb.http.js.JsCmds
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers.nextFuncName
import net.liftweb.http.{S, SHtml}
import collection.mutable.ListBuffer
import com.owlunit.service.cinema._
import stub._
import xml.{Null, Attribute, NodeSeq, Text}
import com.owlunit.web.config.DependencyFactory

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Crud {

  lazy val cinemaService = DependencyFactory.cinemaService.make openOr CinemaServiceStub
  lazy val log = Logger(this.getClass)

  def createKeyword = "input" #> SHtml.text("", (name: String) => {
      println(name)
      cinemaService.loadKeyword(name) match {
        case Some(k) => SnippetUtils.errorBox("Keyword with this name already exists! id = " + k.id, "admin")
        case None => {
          val k = cinemaService.createKeyword(name)
          SnippetUtils.noticeBox("Keyword created with id = " + k.id, "admin")
        }
      }
    })


}