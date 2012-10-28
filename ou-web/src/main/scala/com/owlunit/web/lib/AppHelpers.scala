package com.owlunit.web.lib

import scala.xml._

import net.liftweb._
import common._
import http.{SHtml, S}
import json._
import sitemap.Menu
import util.Helpers._


trait AppHelpers {

  def boolToBox(b: Boolean) = if (b) Full(b) else Empty

  def url(menu: Menu) = S.contextPath + menu.loc.calcDefaultHref

  def ajaxForm(form: NodeSeq) = SHtml.ajaxForm((
    "type=submit [class+]" #> "btn btn-primary"
    )(form))

  def extractString(value: JValue, func: JValue => JValue): Box[String] =
    tryo {
      func(value).values.asInstanceOf[String]
    }

}
