package com.owlunit.web.snippet.admin

import scala.xml.NodeSeq

import net.liftweb._
import common._
import http.js.JsCmds
import http.{Factory, NoticeType, S}
import util.Helpers._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object BootstrapAlerts extends Factory with Loggable {

  val id = "globalalerts"

  def noticeClass(noticeType: NoticeType.Value): String = noticeType match {
    case NoticeType.Notice => "alert-info"
    case NoticeType.Error  => "alert-error"
    case _ => ""
  }

  def noticeTitle(noticeType: NoticeType.Value): NodeSeq = noticeType match {
    case NoticeType.Notice => NodeSeq.Empty
    case NoticeType.Error => <strong>ERROR:</strong>
    case NoticeType.Warning => <strong>Warning:</strong>
  }

  def messages(showAll: Boolean) = if (showAll) S.messages _ else S.noIdMessages _

  def message(msgType: NoticeType.Value, msg: NodeSeq) =
    <div class={"alert %s" format noticeClass(msgType)} data-alert="">
      <a class="close" data-dismiss="alert" href="#">×</a>
      {noticeTitle(msgType)} {msg}
    </div>
  
  def render = {

    val showAll = toBoolean(S.attr("showAll") or S.attr("showall"))

    messages(showAll)(S.errors).map(message(NoticeType.Error, _)) ++
      messages(showAll)(S.warnings).map(message(NoticeType.Warning, _)) ++
      messages(showAll)(S.notices).map(message(NoticeType.Notice, _))

  }

}
