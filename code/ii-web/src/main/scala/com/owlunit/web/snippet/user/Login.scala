package com.owlunit.web.snippet.user

import com.owlunit.web.config.Site
import com.owlunit.web.lib.{Gravatar, AppHelpers}
import com.owlunit.web.model.{User, LoginCredentials}

import scala.xml._

import net.liftweb._
import builtin.snippet.Loc
import common._
import http._
import sitemap.Menu
import util._
import Helpers._

/**
 * Snippet object that configures template-login and connects it to Login.auth
 */
object Login2 {

  private object user extends RequestVar("")
  private object pass extends RequestVar("")

  def url(menu: Menu) = S.contextPath + menu.loc.calcDefaultHref

  def auth() = {
    println("[Login.auth] enter.")

    S.redirectTo(url(Site.home))

    println("[Login.auth] exit.")
  }

  /**
   * This is the part of the snippet that creates the form elements and connects the client side components to
   * server side handlers.
   *
   * @param xhtml - the raw HTML that we are going to be manipulating.
   * @return NodeSeq - the fully rendered HTML
   */
  def login(xhtml: NodeSeq): NodeSeq = {
    println("[Login.login] enter.")
    SHtml.ajaxForm(
      bind("login", xhtml,
        "user" -> SHtml.text(user.is, user(_), "maxlength" -> "40"),
        "pass" -> SHtml.password(pass.is, pass(_)),
        "submit" -> (SHtml.hidden(auth) ++ <input type="submit" class="btn btn-primary" value="Login"/>)))
  }
}

class Login extends StatefulSnippet with AppHelpers with Loggable {

  def url(menu: Menu) = S.contextPath + menu.loc.calcDefaultHref

  def dispatch = { case "render" => render }

  // form vars
  private var password = ""

  def render = {
    "#login_email [value]" #> User.loginCredentials.is.email &
      "#login_password" #> SHtml.password(password, password = _) &
      "#login_submit" #> SHtml.onSubmitUnit(process _) &
      "#login_cancel" #> SHtml.onSubmitUnit(cancel)
  }

  private def getUser: Box[User] =
    for {
      emailParam <- S.param("email") ?~! "Please enter an email address"
      user <- User.findByEmail(emailParam.toLowerCase.trim) ?~! "User with this email not found"
    } yield {
      user
    }

  private def process() = {
    getUser match {
      case Full(user) if (user.password.isMatch(password)) => {
        User.logUserIn(user, true)
        User.createExtSession(user.id.is)
      }
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("User not found")
    }
    S.redirectTo(S.uri)
  }

  private def cancel() = S.seeOther(url(Site.home))
}








