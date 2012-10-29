package com.owlunit.web.snippet

import com.owlunit.web.model._
import net.liftweb.util.Helpers._
import com.owlunit.web.lib.AppHelpers
import net.liftweb.common.{Loggable, Full, Box}
import net.liftweb.http.S
import com.foursquare.rogue.Rogue._


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object MovieSnippet extends AppHelpers with Loggable {

  def current: Box[Movie] = for {
    id <- S.param("id") ?~ "You must provide an id"
    movie <- Movie.find(id)
  } yield { movie }

  def keywords(movie: Movie) = {
    val keywords = Keyword where (_.id in movie.keywords.is) fetch()
    ".key *" #> (".caption *" #> "Keywords" & ".counter *" #> keywords.length) &
      "ul *" #> ("li *" #> keywords.map(_.snippet))
  }

  def crew(movie: Movie, caption: String, role: Role.Role) = {
    val personIds = movie.persons.is.filter(_.role.is == role).map(_.person.is)
    val persons = Person where (_.id in personIds) fetch()
    ".key *" #> (".caption *" #> caption & ".counter *" #> persons.length) &
      "ul *" #> ("li *" #> persons.map(_.snippet))
  }

  def header(movie: Movie) =
    ".name *" #> movie.snippet &
      ".picture *"          #> ("* [src]" #> movie.posterUrl) &
      ".wallpaper [style]"  #> ("background: url(%s);" format movie.backdropUrl.is) &
      ".occupation *"       #> movie.tagline.is &
      ".rating [style]"     #> "width: 95%"

  def renderItems(movie: Movie) =
    ".profile-info *" #> (
      "li *" #> List(
        keywords(movie),
        crew(movie, "Actors",    Role.Actor),
        crew(movie, "Directors", Role.Director),
        crew(movie, "Producer",      Role.Producer)
      ))

  def render = current match {
    case Full(movie) => header(movie) & renderItems(movie)
    case _ => "*" #> (xhtml => xhtml)
  }
}