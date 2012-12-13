package com.owlunit.web.snippet

import com.owlunit.web.model._
import net.liftweb.util.Helpers._
import com.owlunit.web.lib.AppHelpers
import net.liftweb.common.{Loggable, Full, Box}
import net.liftweb.http.S
import com.owlunit.web.config.TMDBConfig


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
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

  def crew(persons: Seq[Person], caption: String) = {
    ".key *" #> (".caption *" #> caption & ".counter *" #> persons.length) &
      "ul *" #> ("li *" #> persons.map(_.snippet))
  }

  def allCrew(movie: Movie) = List(
    crew(movie.cast.is.map(_.person.obj).flatten, "Cast"),
    crew(movie.crew.is.map(_.person.obj).flatten, "Crew")
  )

  def header(movie: Movie) =
    ".name *"               #> movie.snippet &
      ".year *"             #> movie.release.is.toString &
      ".picture *"          #> ("* [src]" #> (TMDBConfig.baseUrl + "w154" + movie.posterUrl.is)) &
      ".wallpaper [style]"  #> ("background: url(%s);" format (TMDBConfig.baseUrl + "w1280" + movie.backdropUrl.is)) &
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