package com.owlunit.web.snippet

import admin.AdminHelper
import net.liftweb.util.Helpers._
import net.liftweb.http._
import js.JE._
import js.JsCmds._
import js.{JsCmds, JE, JsCmd}
import net.liftweb.common.{Logger, Full, Failure, Empty}
import collection.mutable.ListBuffer
import xml.{Text, NodeSeq}
import org.bson.types.ObjectId
import com.owlunit.web.lib.JsHandlerSnippet

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object Droppable extends JsHandlerSnippet with AdminHelper {

  def handleJs(param: String): JsCmd = {
    param.split("~").toList match {
      case itemId :: _ if ObjectId.isValid(itemId) => items(items.is + itemId)
      case itemId :: _ => items(items.is + itemId)
      case _ => JsLog("Unable to parse param")
    }

    JsLog("drop handled, param = %s, items = %s" format  (param, items.is.toString()))
  }

  def item = "*" #> items.map(item => (
    "* [itemid]" #> item &
    "* *" #> item
    ))

}

object items extends SessionVar(Set[String]())
