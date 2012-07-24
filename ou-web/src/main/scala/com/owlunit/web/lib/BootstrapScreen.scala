package com.owlunit.web.lib

import net.liftweb._
import common._
import http.js.JsCmds
import http.js.JsCmds.Alert
import http.{LiftScreen, S}
import sitemap.Menu
import util.FieldError
import util.Helpers._
import xml.{Elem, NodeSeq}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait BootstrapScreen extends LiftScreen with AppHelpers with Loggable {

  override def allTemplatePath = "templates-hidden" :: "bootstrap-screen" :: Nil

  // make screen ajaxable
  override def calcAjaxOnDone = { logger.trace("Ajax called") }

  // be able to use botstrap's spans, append them from param to all inputs
  def spanClass = "span%s" format (S.attr("span") openOr "4")
  override protected def renderHtml() = ("input [class+]" #> spanClass)(super.renderHtml())

  // make button's captions overridable
  def finishCaption = "Ok"
  def cancelCaption = "Cancel"
  def openCaption = "Open"

  // override buttons
  override val finishButton = <button class="btn btn-primary">{ finishCaption }</button>
  override val cancelButton =
    if (cancelCaption.length > 0) <button class="btn">{ cancelCaption }</button>
    else <span></span> //TODO replace span with smth with more sense

}

trait ModalScreen extends BootstrapScreen {

  // wrap singleton action for latter use
  def divId = FormGUID.get

  // declare template
  override def allTemplatePath = "templates-hidden" :: "modal-screen" :: Nil

  // add modifications to parent's form's content and enclosing div
  override def renderHtml = ("form [class+]" #> "form-horizontal" & "label [class+]" #> "control-label")(super.renderHtml())
  override protected def wrapInDiv(in: NodeSeq) = super.wrapInDiv(in) % ("class" -> "modal fade")

  // declare modal button for rendering
  protected def modalButton = <a class="btn" data-toggle="modal" href={ "#%s" format divId }>{ openCaption }</a>

  // override dispatch, adding href and button for convenience
  override def dispatch = {
    case "modalHref"   => "* [href]" #> ("#%s" format divId) & "* [data-toggle]" #> "modal"
    case "modalButton" => "*" #> modalButton
    case other         => super.dispatch(other)
  }

}

