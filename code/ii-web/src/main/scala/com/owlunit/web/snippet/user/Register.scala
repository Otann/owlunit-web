package com.owlunit.web.snippet.user

import com.owlunit.web.config.Site
import com.owlunit.web.model.User


import net.liftweb._
import common._
import http.{S, SHtml, StatefulSnippet}
import util._
import Helpers._
import com.owlunit.web.lib.AppHelpers

class Register extends StatefulSnippet with AppHelpers with Loggable {

  def dispatch = { case "render" => render }

  // form vars
  private var password = ""
  private var email = ""

  def render = {
    "#signup_email [value]" #> SHtml.text (email, email = _) &
      "#signup_password" #> SHtml.password(password, password = _) &
      "#signup_submit" #> SHtml.onSubmitUnit(process _)
  }

  private def process() = {
    User.findByEmail(email) match {
      case Full(user) => S.error("User exists")
      case Failure(msg, _, _) => S.error(msg)
      case Empty => {

        val user = User.createRecord
        user.email(email).password(password)
        user.password.hashIt
        user.save

        User.logUserIn(user, true)
        User.createExtSession(user.id.is)
        S.notice("Thanks for signing up!")
        S.redirectTo(S.uri)
      }
    }
  }

}








