package com.owlunit.web.lib.ui

import net.liftweb.http._
import js.JE._
import js.JsCmd
import js.JsCmds._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
class CallableFunction(name: String, callback: (String) => JsCmd, args: List[String] = List()) extends JsCmd {
  //  override val toJsCmd = AnonFunc(
  override val toJsCmd = Function(
    name,
    args,
    //  SHtml.jsonCall ?
    SHtml.ajaxCall(JsRaw("Array.prototype.slice.call(arguments).join('|')"), callback)._2
  ).toJsCmd
}