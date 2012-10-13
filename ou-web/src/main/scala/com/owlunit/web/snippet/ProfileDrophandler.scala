package com.owlunit.web.snippet

import net.liftweb.http._
import js.JsCmds._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds.{Function, Script, Run}
import js.JE.{AnonFunc, JsVar, JsRaw}
import xml.NodeSeq
import net.liftweb.http.js.{JsCmd, JsCmds, JE}
import net.liftweb.http.SHtml
import com.owlunit.web.lib.ui.JsHandlerSnippet
import com.owlunit.web.model.User
import net.liftweb.common.{Full, Empty, Failure}
import net.liftweb.json.JsonAST.JObject

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object ProfileDropHandler extends JsHandlerSnippet {

  // This name will be used on client side
  def jsFuncName = "OU.Callbacks.handleProfileDrop"

  // This will be called on server-side
  def handler(rawObj: Any) = User.currentUser match {
    case Empty => Run("alert('no user logged on');")
    case Failure(message, _, _) => Run("alert('error: %s');" format message)
    case Full(user) => {

      // unpack id and type
      val (tagId: String, tagType: String) = Full(rawObj).asA[Map[String, Any]] match {
        case Full(m) => (
          m.get("id").getOrElse("No id"),
          m.get("type").getOrElse("No type")
          )
        case _ => ("No id", "No type")
      }

      tagType match {
        case "keyword" => Run("alert('processing keyword %s');" format tagId)
        case _ => Run("alert('processing %s of type %s');" format (tagId, tagType))
      }


    }
  }

}
