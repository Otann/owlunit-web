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
    movie:Movie <- Movie.find(id)
  } yield { movie }

  def keywords(movie: Movie) = {
    val keywords = movie.keywords
    ".key *" #> (".caption *" #> "Keywords" & ".counter *" #> keywords.length) &
      "ul *" #> ("li *" #> movie.keywords.map(_.snippet))
  }

  def crew(personsMap: Map[Role.Role, Seq[Person]], caption: String, role: Role.Role) = {
    val persons = personsMap(role)
    ".key *" #> (".caption *" #> caption & ".counter *" #> persons.length) &
      "ul *" #> ("li *" #> persons.map(_.snippet))
  }

  def allCrew(movie: Movie) = {
    val persons = movie.persons
    List(
      crew(persons, "Actor", Role.Actor),
      crew(persons, "Director", Role.Director),
      crew(persons, "Producer", Role.Producer)
    )
  }

  def header(movie: Movie) =
    ".name *" #> movie.snippet &
      ".year *" #> movie.year.is &
      ".picture *"          #> ("* [src]" #> movie.posterUrl) &
      ".wallpaper [style]"  #> ("background: url(%s);" format movie.backdropUrl.is) &
      ".occupation *"       #> movie.tagline.is &
      ".rating [style]"     #> "width: 95%"

  def renderItems(movie: Movie) =
    ".profile-info *" #> (
      "li *" #> keywords(movie) :: allCrew(movie)
      )

  def render = current match {
    case Full(movie) => header(movie) & renderItems(movie)
    case _ => "*" #> (xhtml => xhtml)
  }
}