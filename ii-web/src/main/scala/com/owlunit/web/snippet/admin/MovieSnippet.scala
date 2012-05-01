package com.owlunit.web.snippet.admin

import net.liftweb.util.Helpers._
import net.liftweb.http._
import js.JE._
import js.JsCmd
import js.JsCmds._
import xml.NodeSeq
import com.owlunit.web.model.Movie
import net.liftweb.common._
import com.owlunit.web.lib.BootstrapScreen

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MovieSnippet {

  def current: Box[Movie] = for {
    id <- S.param("id") ?~ "You must provide an id"
    movie <- Movie.findById(id)
  } yield { movie }
  
  def name = "* *" #> current.map(_.name.is)

  def edit = "* *" #> EditMovieScreen.toForm

}

object EditMovieScreen extends BootstrapScreen {
  override def screenTop = Full(<h3>Edit Movie</h3>)

  object movieVar extends ScreenVar(MovieSnippet.current openOr Movie.createRecord)
  addFields(() => movieVar.is.editFields)

  protected def finish() {
    movieVar.is.save
    S.seeOther(S.referer openOr "")
  }
}

