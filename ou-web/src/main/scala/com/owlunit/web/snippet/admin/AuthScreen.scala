package com.owlunit.web.snippet.admin

import com.owlunit.web.config.Site
import com.owlunit.web.model._


import net.liftweb._
import common.{Logger, Empty, Failure, Full}
import http.js.JsCmds
import http.js.JsCmds.Alert
import http.{LiftScreen, S}
import sitemap.Menu
import util.FieldError
import util.Helpers._
import xml.NodeSeq
import com.owlunit.web.lib.{ModalScreen, AppHelpers, Gravatar}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object LoginScreen extends ModalScreen {

  override def screenTop = Full(<h3>{ "Welcome back" }</h3>)
  override def finishCaption = "Login"
  override def cancelCaption = "Cancel"

  val mailField = field("Email", "")
  val passField = password("Password", "")

  override def validations = auth _ :: super.validations

  def auth(): Errors = {
    val email = mailField.is
    val pass = passField.is

    User.findByEmail(email) match {
      case Empty => List(FieldError(mailField, "There is no user with such email"))
      case Full(user) if (!user.password.isMatch(pass)) => List(FieldError(passField, "Wrong password"))
      case _ => List.empty
    }
  }

  override def calcAjaxOnDone = {
    User.findByEmail(mailField.is) match {
      case Full(user) => {
        User.logUserIn(user, true)
        User.createExtSession(user.id.is)
        S.seeOther(url(Site.home))
      }
      case _ => {
        S.notice("Unknown error")
      }
    }
  }

  protected def finish() { }
}

object RegisterScreen extends ModalScreen {

  object userVar extends ScreenVar(User.createRecord)

  override def screenTop = Full(<h3>{ "Welcome" }</h3>)
  override def finishCaption = "Register"
  override def cancelCaption = "Cancel"

  addFields(() => userVar.is.registerScreenFields)

  override def calcAjaxOnDone = {
    val email = userVar.is.email.is
    User.findByEmail(email) match {
      case Full(user) => S.error("User with this email already exists")
      case Failure(msg, _, _) => S.error(msg)
      case Empty => {
        val user = userVar.is
        user.password.hashIt
        user.save
        User.logUserIn(user, true)
        User.createExtSession(user.id.is)
        S.notice("Thanks for signing up!")
        JsCmds.RedirectTo(Referer.get, () => S.appendNotices(S.getAllNotices))
      }
    }
  }

  protected def finish() {
    debug("registration done")
  }
}






