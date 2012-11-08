package com.owlunit.web.lib.ui

import net.liftweb._
import common._
import http.{LiftScreen, S}
import util.Helpers._
import xml.{Node, Text, Elem, NodeSeq}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Comfortable way to use lift screen with bootstrap
 */
trait BootstrapScreen extends LiftScreen with Loggable {

  override def allTemplatePath = "templates-hidden" :: "screen-bootstrap" :: Nil

  // makes screen ajaxable
  override def calcAjaxOnDone = {
    logger.trace("Ajax called")
  }

  // be able to use bootstrap spans, append them from param to all inputs
  def spanClass = "span%s" format (S.attr("span") openOr "4")
  override protected def renderHtml() = ("input [class+]" #> spanClass)(super.renderHtml())
  override protected def wrapInDiv(in: NodeSeq) = super.wrapInDiv(in) % ("class" -> spanClass)

  // make button's captions overridable
  def finishCaption = "Ok"
  def cancelCaption = "Cancel"

  // override buttons
  override val finishButton = <button class="btn btn-primary">{finishCaption}</button>

  override val cancelButton: Elem =
    if (cancelCaption.length > 0)
      <button class="btn">
        {cancelCaption}
      </button>
    else
      <button style="display: none;"></button>

}


