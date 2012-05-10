package com.owlunit.web.lib

import net.liftweb._
import common.{Logger, Empty, Failure, Full}
import http.js.JsCmds
import http.js.JsCmds.Alert
import http.{LiftScreen, S}
import sitemap.Menu
import util.FieldError
import util.Helpers._
import xml.NodeSeq

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait BootstrapScreen extends LiftScreen with AppHelpers with Logger {

  override def allTemplatePath = "templates-hidden" :: "bootstrap-screen" :: Nil

  override def calcAjaxOnDone = { debug("Ajax called") }

  def spanClass = "span%s" format (S.attr("span") openOr "4")
  override protected def renderHtml() = ("input [class+]" #> spanClass)(super.renderHtml())

  def finishCaption = "Ok"
  def cancelCaption = "Cancel"
  def openCaption = "Open"

  override val finishButton = <button class="btn btn-primary">{ finishCaption }</button>
  override val cancelButton = <button class="btn">{ cancelCaption }</button>

}

trait ModalScreen extends BootstrapScreen {


  def divId = FormGUID.get

  override def allTemplatePath = "templates-hidden" :: "modal-screen" :: Nil
  override def renderHtml = ("form [class+]" #> "form-horizontal" & "label [class+]" #> "control-label")(super.renderHtml())
  override protected def wrapInDiv(in: NodeSeq) = super.wrapInDiv(in) % ("class" -> "modal fade")

  protected def modalButton = <a class="btn" data-toggle="modal" href={ "#%s" format divId }>{ openCaption }</a>

  override def dispatch = {
    case "modalHref"   => "* [href]" #> "#%s".format(divId) & "* [data-toggle]" #> "modal"
    case "modalButton" => "*" #> modalButton
    case other         => super.dispatch(other)
  }

}

