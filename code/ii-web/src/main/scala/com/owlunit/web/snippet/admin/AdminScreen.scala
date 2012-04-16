package com.owlunit.web.snippet.admin

import com.owlunit.web.config.Site
import com.owlunit.web.model._

import scala.xml._

import net.liftweb._
import common.{Full, Box}
import http.js.JsCmd
import http.js.JsCmds.Alert
import http.{AbstractScreen, ScreenFieldInfo, LiftScreen, S}
import sitemap.Menu
import util.FieldError
import util.Helpers._
import com.owlunit.web.lib.{AppHelpers, Gravatar}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait AdminScreen extends LiftScreen {

  def finishCaption = "Ok"
  def cancelCaption = "Cancel"
  def openCaption = "Open"

  val divId = nextFuncName

  override def allTemplatePath = "templates-hidden" :: "modal-screen" :: Nil
  override protected def allTemplate = ("#modal_id [id]" #> divId)(super.allTemplate)

  override val finishButton = <button class="btn btn-primary">{ finishCaption }</button>
  override val cancelButton = <button class="btn">{ cancelCaption }</button>

  protected def modalButton = <a class="btn" data-toggle="modal" href={ "#%s" format divId }>{ openCaption }</a>

  override def dispatch = {
    case "modalButton" => "*" #> modalButton
    case other         => super.dispatch(other)
  }


}

object TestAdminScreen extends AdminScreen {

  def url(menu: Menu) = S.contextPath + menu.loc.calcDefaultHref

  val flavour = field("What's your favorite Ice cream flavor", "", trim, valMinLen(2, "Name too short"))
  val sauce = field("Like chocolate sauce?", false)

  override def calcAjaxOnDone = {
    println("Flavour = " + flavour.is)
    Alert("Flavour = " + flavour.is)
  }

  override def screenTop = Full(<h3>{ "Test Admin Screem" }</h3>)

  override def localSetup {
    Referer(url(Site.home))
  }

  protected def finish() {
    println("Done")
    println("Flavour = " + flavour.is)
    S.notice("Well done")
  }

}