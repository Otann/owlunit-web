package com.owlunit.web.lib.ui

import net.liftweb._
import util.Helpers._
import xml.NodeSeq

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Wraps screen in bootstrap modal functionality
 */
trait ModalScreen extends BootstrapScreen {

  // alias for form's ScreenVar
  def divId = FormGUID.get

  override def allTemplatePath = "templates-hidden" :: "screen-modal" :: Nil

  def openCaption   = "Open"

  // add modifications to parent's form's content and enclosing div
  override def renderHtml() = (
    "form  [class+]" #> "form-horizontal" &
    "label [class+]" #> "control-label"
    ) (super.renderHtml())

  // add class to enclosing div
  override protected def wrapInDiv(in: NodeSeq) = super.wrapInDiv(in) % ("class" -> "modal fade")

  // declare modal button for rendering
  protected def modalButton = <a class="btn" data-toggle="modal" href={"#%s" format divId}> {openCaption} </a>

  // override dispatch, adding href and button for convenience
  override def dispatch = {
    case "modalHref"   => "* [href]" #> ("#%s" format divId) & "* [data-toggle]" #> "modal"
    case "modalButton" => "*" #> modalButton
    case other => super.dispatch(other)
  }

}
