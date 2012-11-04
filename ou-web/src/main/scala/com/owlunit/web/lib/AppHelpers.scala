package com.owlunit.web.lib

import scala.xml._

import net.liftweb._
import common._
import http.js.{JsCmds, JsCmd}
import http.{SHtml, S}
import json._
import sitemap.Menu
import util.Helpers._


trait AppHelpers {

  def boolToBox(b: Boolean) = if (b) Full(b) else Empty

  def ajaxForm(form: NodeSeq) = SHtml.ajaxForm((
    "type=submit [class+]" #> "btn btn-primary"
    )(form))

  def extractString(value: JValue, func: JValue => JValue): Box[String] =
    tryo {
      func(value).values.asInstanceOf[String]
    }

  def JsLog(log: String):JsCmd = JsCmds.Run("console.log('%s');" format log.replace("\n", ""))
  def JsLog(logs: Any*):JsCmd = JsLog(logs.mkString(" "))

}
