package com.owlunit.web.snippet.admin

import net.liftweb.util.Helpers._
import net.liftweb.http._
import com.owlunit.web.model.Movie
import net.liftweb.common._
import com.owlunit.web.lib.ui.BootstrapScreen

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MovieInfo {

  def current: Box[Movie] = for {
    id <- S.param("id") ?~ "You must provide an id"
    movie <- Movie.find(id)
  } yield { movie }

  def render = "movie-data=name *" #> current.map(_.name.is) &
      "movie-data=year *" #> current.map(_.year.is.toString) &
      "movie-data=poster [src]" #> current.map(_.posterUrl.is)


}

object EditMovieScreen extends BootstrapScreen {
  override def screenTop = Full(<h3>Edit Movie</h3>)

  object movieVar extends ScreenVar(MovieInfo.current openOr Movie.createRecord)
  addFields(() => movieVar.is.editFields)

  protected def finish() {
    movieVar.is.save
    S.seeOther(S.referer openOr "")
  }
}

