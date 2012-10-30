package com.owlunit.web.snippet

import com.owlunit.web.model.User
import net.liftweb.util.Helpers._
import com.owlunit.web.config.Site
import com.owlunit.web.lib.AppHelpers
import net.liftweb.common.{Loggable, Full}
import com.owlunit.web.model.common.IiTagRecord

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object MeSnippet extends AppHelpers with Loggable {

  def logout = "* [href]" #> url(Site.logout)

  def smallAvatar = "* [src]" #> User.currentUser.map(_.photo.is)

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
    case Full(user) => {
      logger.debug("\n\n!!!!!!!!!!!!!!!!!!!!!\n\n")
      logger.debug("Keywords: %s" format user.keywords)
      logger.debug("Raw: %s" format user.ii.items.keys.filter(_.meta("ii.cinema.iiType") == "keyword").map(_.meta))
      logger.debug("\n\n!!!!!!!!!!!!!!!!!!!!!\n\n")
      logger.debug("Movies: %s" format user.movies)
      logger.debug("Raw: %s" format user.ii.items.keys.filter(_.meta("ii.cinema.iiType") == "movie").map(_.meta))
      logger.debug("\n\n!!!!!!!!!!!!!!!!!!!!!\n\n")
      renderInfo(user) & renderItems(user)
    }
    case _ => "*" #> (xhtml => xhtml)
  }
}