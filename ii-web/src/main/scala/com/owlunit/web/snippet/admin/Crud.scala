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
import net.liftweb.util.Helpers.nextFuncName
import collection.mutable.ListBuffer
import com.owlunit.service.cinema._
import stub._
import xml.{Null, Attribute, NodeSeq, Text}
import com.owlunit.web.config.DependencyFactory
import net.liftweb.http.{SessionVar, SHtml, S}
import net.liftweb.http.js.jquery.JqJsCmds.FadeIn

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Crud {

  lazy val cinemaService = DependencyFactory.cinemaService.make openOr CinemaServiceStub
  lazy val log = Logger(this.getClass)

  def createKeyword = "input" #> SHtml.text("", (name: String) => {
    println(name)
    try {
      cinemaService.loadKeyword(name) match {
        case Some(k) => S.error("Keyword with this name already exists! id = %d" format k.id)
        case None => {
          val k = cinemaService.createKeyword(name)
          S.notice("Keyword created with id = %d" format k.id)
        }
      }
    } catch {
      case ex: Exception => {JsCmds.Alert("asdasd"); S.notice("Keyword NOT created"); S.error(ex.getMessage)}
    }
    S.notice("NOTICE")
  })

  object ExampleVar extends SessionVar[String]("Replace Me")
  def two(xhtml: NodeSeq): NodeSeq = SHtml.ajaxEditable(
    Text(ExampleVar.is),
    SHtml.text(ExampleVar.is, ExampleVar(_)),
    () => FadeIn("example_two_notice")
  )
}