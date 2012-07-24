package com.owlunit.web.snippet.admin

import net.liftweb.util.Helpers._
import net.liftweb.http._
import js.JE._
import js.JsCmd
import js.JsCmds._
import xml.NodeSeq
import com.owlunit.web.model.Movie
import net.liftweb.common._
import com.owlunit.web.lib.{Gravatar, JsHandlerSnippet, BootstrapScreen}
import com.owlunit.web.lib.{JsHandlerSnippet, Gravatar, BootstrapScreen}
import com.owlunit.web.model.User

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
object ProfileInfo extends Loggable {

  def render = User.currentUser match {
    case Full(user) =>
      "profile-data=name *" #> user.username.is &
        "profile-data=bio *" #> user.bio.is &
        "profile-data=pic [src]" #> Gravatar.imageUrl(user.email.is)
    case _ => (x: NodeSeq) => x
  }

}

object ProfileDroppable extends JsHandlerSnippet with AdminHelper {

  def handleJs(param: String) = null

}

object ProfileItems extends SessionVar(Set[String]())