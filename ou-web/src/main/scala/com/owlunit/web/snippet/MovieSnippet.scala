package com.owlunit.web.snippet

import com.owlunit.web.model._
import net.liftweb.util.Helpers._
import com.owlunit.web.config.Site
import com.owlunit.web.lib.ui.Gravatar
import com.owlunit.web.lib.AppHelpers
import net.liftweb.common.{Full, Box, Logger}
import net.liftweb.http.S
import xml.NodeSeq
import net.liftweb.util.{CssSel, PassThru}
import com.foursquare.rogue.Rogue._
import org.bson.types.ObjectId


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object MovieSnippet extends AppHelpers with Logger {

  def current: Box[Movie] = for {
    id <- S.param("id") ?~ "You must provide an id"
    movie <- Movie.find(id)
  } yield { movie }

  def titleTag = ".name *" #> current.map(_.snippet)

  def keywords(movie: Movie) = {
    val keywords = Keyword where (_.id in movie.keywords.is) fetch()
    ".key *" #> "Keywords" & "ul *" #> ("li *" #> keywords.map(_.snippet))
  }

  def crew(movie: Movie, caption: String, role: Role.Role) = {
    val actorsIds = movie.persons.is.filter(_.role.is == role).map(_.person.is)
    val actors = Person where (_.id in actorsIds) fetch()
    ".key *" #> caption & "ul *" #> ("li *" #> actors.map(_.snippet))
  }

  def header = titleTag &
    ".picture *" #> ("* [src]" #> current.map(_.posterUrl)) &
    ".wallpaper [style]" #> current.map("background: url(%s);" format _.backdropUrl.is) &
    ".occupation *" #> current.map(_.tagline.is) &
    ".rating [style]" #> "width: 95%"

  def info = current match {
    case Full(movie) => ".profile-info *" #> ("li *" #> List(
      keywords(movie),
      crew(movie, "Actors", Role.Actor),
      crew(movie, "Directors", Role.Director),
      crew(movie, "Producers", Role.Producer)
    ))
    case _ => "*" #> (xhtml => xhtml)
  }

  def render = header & info
}