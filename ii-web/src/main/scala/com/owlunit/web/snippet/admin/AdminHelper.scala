package com.owlunit.web.snippet.admin

import xml.NodeSeq
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.NoticeType
import net.liftweb.common.Logger

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait AdminHelper extends Logger {

  def item(name: String, itemId: String):NodeSeq = <span itemId={ itemId } class="ii badge label">{ name }</span>

  def JsLog(log: String):JsCmd = JsCmds.Run("console.log('%s');" format log.replace("\n", ""))
  def JsLog(logs: Any*):JsCmd = JsLog(logs.mkString(" "))
  def jsAlert(msgType: NoticeType.Value, msg: NodeSeq) = {
    JsCmds.Run("$$('#jsalerts').append('%s')" format BootstrapAlerts.message(msgType, msg).toString().replace("\n", ""))
  } //TODO dirty jquery

}