package com.owlunit.web.lib

import net.liftweb.util.Helpers._
import net.liftweb.http._
import js.JE._
import js.JsCmd
import js.JsCmds._
import xml.NodeSeq

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
class CallableFunction(name:String, callback:(String)=>JsCmd, args:List[String] = List()) extends JsCmd {
//  override val toJsCmd = AnonFunc(
  override val toJsCmd = Function(
    name,
    args,
//  SHtml.jsonCall
    SHtml.ajaxCall(JsRaw("Array.prototype.slice.call(arguments).join('|')"), callback)._2
  ).toJsCmd
}

trait JsHandlerSnippet {

  val defaultFuncName = nextFuncName

  // This will be called on server-side
  def handleJs(param: String): JsCmd

  def script(x: NodeSeq): NodeSeq = {
    val funcName = S.attr("name") openOr defaultFuncName
    Script(ensure & OnLoad(SetExp(JsVar("window.lift", funcName), jsFunc)))
  }

  val params = "params"
  val jsFunc = AnonFunc(params, SHtml.ajaxCall(JsVar(params), handleJs _)._2)
  val ensure = JsRaw("if (typeof window.lift == 'undefined') window.lift = {}")

}

trait handleDrop extends  JsHandlerSnippet {
  override val defaultFuncName = "handleDrop"
}