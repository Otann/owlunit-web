package com.owlunit.web.snippet

import com.owlunit.web.model.{Movie, User}
import net.liftweb.util.Helpers._
import com.owlunit.web.config.{TMDBConfig, Site}
import com.owlunit.web.lib.{RecommendationEngine, AppHelpers}
import net.liftweb.common.{Box, Loggable, Full}
import com.owlunit.web.model.common.IiTagRecord
import net.liftweb.http.S
import org.bson.types.ObjectId
import com.owlunit.web.lib.ui.IiTag
import com.owlunit.core.ii.mutable.Ii

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object RecoSnippet extends Loggable {

  def renderMovie(movie: Movie) = ".title *" #> movie.snippet &
    ".rating [style]" #> "width: 70%" &
    ".poster [style]" #> ("background: url(%s)" format (TMDBConfig.baseUrl + "w1280" + movie.backdropUrl.is)) &
    ".tags *" #> ("* *" #> movie.keywords.map(_.snippet))

  def query: List[IiTag] = {
    val ids = S.param("query") match {
      case Full(query) => query.split(' ').toList
      case _           => List()
    }
    ids.map(id => IiTagRecord.load(id)).flatten
  }

  def renderQuery = "*" #> query.map(_.snippet)

  def render = {

    val iiRecords: Seq[Box[IiTagRecord[_]]] = User.currentUser :: query.map(IiTagRecord.load(_))
    val result = RecommendationEngine.recommend(iiRecords.flatten)

    "* *" #> result.map(renderMovie(_))
  }

}
