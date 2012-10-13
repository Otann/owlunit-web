package com.owlunit.web.lib.ui

import net.liftweb.util.Helpers._
import net.liftweb.http._
import js.JE._
import js.{JsCmds, JsCmd}
import js.JsCmds._
import xml.NodeSeq
/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait JsHandlerSnippet {

  // This name will be used on client side
  def jsFuncName: String

  // This will be called on server-side
  def handler(parameter: Any): JsCmd

  def jsFunc = AnonFunc("argument", SHtml.jsonCall(JsVar("argument"), handler _)._2)

  def render(x: NodeSeq): NodeSeq = JsCmds.Script(SetExp(JsVar(jsFuncName), jsFunc))

}