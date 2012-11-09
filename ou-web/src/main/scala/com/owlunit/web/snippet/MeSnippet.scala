package com.owlunit.web.snippet

import com.owlunit.web.model.User
import net.liftweb.util.Helpers._
import com.owlunit.web.config.Site
import com.owlunit.web.lib.AppHelpers
import net.liftweb.common.{Loggable, Full}
import com.owlunit.web.model.common.IiTagRecord

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */


object MeSnippet extends AppHelpers with Loggable {

  def logout = "* [href]" #> Site.logout.url

  def photo = "* [src]" #> User.currentUser.map(_.photo.is)

  def username = "* *" #> User.currentUser.map(_.name)

  def renderItemsList(user: User, caption: String, items: List[IiTagRecord[_]]) = {
    ".key *" #> (".caption *" #> caption & ".counter *" #> items.length) &
      "ul *" #> ("li *" #> items.map(_.snippet))
  }

  def renderItems(user: User) =
    ".profile-info *" #> (
      "li *" #> List(
        renderItemsList(user, "Movie", user.movies),
        renderItemsList(user, "Persons", user.persons),
        renderItemsList(user, "Keywords", user.keywords)
      ))

  def renderInfo(user: User) =
    ".name *"                  #> user.snippet &
      ".picture-image   [src]" #> user.photo.is &
      ".wallpaper-image [src]" #> user.cover.is &
      ".occupation *"          #> user.bio.is &
      ".rating [style]"        #> ("width: %d%%" format 95)

  def render = User.currentUser match {
    case Full(user) => renderInfo(user) & renderItems(user)
    case _ => "*" #> (xhtml => xhtml)
  }
}