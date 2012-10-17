package com.owlunit.web.lib

import scala.xml._

import net.liftweb._
import common._
import http.{SHtml, S, NoticeType}
import json._
import sitemap.Menu
import util.CssSel
import util.Helpers._

import org.bson.types.ObjectId

trait AppHelpers {

  def boolToBox(b: Boolean) = if (b) Full(b) else Empty

  def url(menu: Menu) = S.contextPath + menu.loc.calcDefaultHref

  def ajaxForm(form: NodeSeq) = SHtml.ajaxForm((
    "type=submit [class+]" #> "btn btn-primary"
    )(form))

}
