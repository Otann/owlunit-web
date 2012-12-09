package com.owlunit.web.lib

import scala.xml._

import net.liftweb._
import common._
import common.Full
import http.js.{JsCmds, JsCmd}
import http.{SHtml, S}
import json._
import sitemap.Menu
import util.Helpers._
import com.ning.http.client.RequestBuilder
import dispatch._
import scala.Left
import scala.Right
import java.net.ConnectException

object AppHelpers extends AppHelpers

trait AppHelpers {

  def boolToBox(b: Boolean) = if (b) Full(b) else Empty

  def ajaxForm(form: NodeSeq) = SHtml.ajaxForm((
    "type=submit [class+]" #> "btn btn-primary"
    )(form))

  def extractString(value: JValue, func: JValue => JValue): Box[String] =
    tryo {
      func(value).values.asInstanceOf[String]
    }

  def extractInt(value: JValue, func: JValue => JValue): Box[Int] =
    tryo {
      func(value).values.asInstanceOf[scala.math.BigInt].toInt
    }

  def JsLog(log: String):JsCmd = JsCmds.Run("console.log('%s');" format log.replace("\n", ""))
  def JsLog(logs: Any*):JsCmd = JsLog(logs.mkString(" "))

  protected def requestJSON(req: RequestBuilder): Box[JValue] =
    Http(req OK as.String).either() match {
      case Right(value)    =>           Full(JsonParser.parse(value))
      case Left(StatusCode(404))     => Failure("Face from from %s" format req, Empty, Empty)
      case Left(x: ConnectException) => Failure("Connection Expection: %s" format x, Empty, Empty)
      case Left(throwable) =>           Failure("Bad answer from %s" format req, Full(throwable), Empty)
    }

}


