package com.owlunit.web.snippet.admin

import com.owlunit.web.config.Site
import com.owlunit.web.model._

import scala.xml._

import net.liftweb._
import common.{Empty, Failure, Full, Box}
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

  override val finishButton = <button class="btn">{ finishCaption }</button>
  override val cancelButton = <button class="btn">{ cancelCaption }</button>

  protected def modalButton = <a class="btn" data-toggle="modal" href={ "#%s" format divId }>{ openCaption }</a>

  override def dispatch = {
    case "modalHref"   => "* [href]" #> "#%s".format(divId) & "* [data-toggle]" #> "modal"
    case "modalButton" => "*" #> modalButton
    case other         => super.dispatch(other)
  }

  def url(menu: Menu) = S.contextPath + menu.loc.calcDefaultHref

}

class RegisterScreen extends AdminScreen {

  override def screenTop = Full(<h3>{ "Welcome" }</h3>)
  override def finishCaption = "Register"
  override def cancelCaption = "Cancel"

//  addFields(() => User.registerScreenFields)
  val email = field(User.email)
  val pass  = field(User.password)

  override def calcAjaxOnDone = {
    val email: String = field(User.email).is
    val pass: String = field(User.password).is
    User.findByEmail(email) match {
      case Full(user) => S.error("User with this email already eists")
      case Failure(msg, _, _) => S.error(msg)
      case Empty => {
        val user = User.createRecord
        user.email(email).password(pass)
        user.password.hashIt
        user.save
        User.logUserIn(user, true)
        User.createExtSession(user.id.is)
        S.notice("Thanks for signing up!")
      }
    }
    Alert("asdads")
  }

  protected def finish() {
    S.notice("Email = " + field(User.email).is)
    Alert("asdads")
  }
}