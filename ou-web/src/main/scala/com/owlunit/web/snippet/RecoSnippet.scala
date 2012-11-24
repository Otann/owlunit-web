package com.owlunit.web.snippet

import com.owlunit.web.model.{Movie, User}
import net.liftweb.util.Helpers._
import com.owlunit.web.config.Site
import com.owlunit.web.lib.{RecommendationEngine, AppHelpers}
import net.liftweb.common.{Box, Loggable, Full}
import com.owlunit.web.model.common.IiTagRecord
import net.liftweb.http.S
import org.bson.types.ObjectId
import com.owlunit.web.lib.ui.IiTag

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object RecoSnippet extends Loggable {

  def renderMovie(movie: Movie) = ".title *" #> movie.snippet &
    ".rating [style]" #> "width: 70%" &
    ".poster [style]" #> ("background: url(%s)" format movie.backdropUrl) &
    ".tags *" #> (movie.keywords.map(_.snippet))

  def query: Seq[IiTag] = {
    val ids: Seq[String] = S.param("query") match {
      case Full(query) => query.split(' ')
      case _           => Seq()
    }
    ids.map(id => IiTagRecord.load(id)).flatten
  }

  def renderQuery = "*" #> query.map(_.snippet)

  def render = {
    val iiRecords = query.map(IiTagRecord.load(_))
    val result = RecommendationEngine.recommend(iiRecords.flatten)

    "* *" #> result.map(renderMovie(_))
  }

}
