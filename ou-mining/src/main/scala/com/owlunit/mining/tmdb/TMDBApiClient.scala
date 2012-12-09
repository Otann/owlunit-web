package com.owlunit.mining.tmdb

import dispatch._
import net.liftweb.common._
import net.liftweb.json._
import net.liftweb.util.Props
import com.ning.http.client.RequestBuilder
import net.liftweb.common.Full
import net.liftweb.util.Helpers._

import com.owlunit.web.model.Movie
import com.owlunit.web.lib.AppHelpers
import com.foursquare.rogue.Rogue._

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object TMDBApiClient extends AppHelpers with Loggable {

  def key = Props.get("tmdb.key", "N/A")
  def root = host("api.themoviedb.org") / "3"

  def request(path: RequestBuilder => RequestBuilder) = path(root) addQueryParameter("api_key", key)

  def getConfig = requestJSON(root / "configuration")

  def updateMovie(tmdbId: Long): Box[Movie] = {

    requestJSON(
      request(_ / "movie" / tmdbId.toString addQueryParameter("append_to_response", "trailers,images,casts,keywords"))
    ) match {
      case Full(raw) => Extractor.extractMovie(raw)
      case _         => Empty
    }

  }

  def fetchNewMovie(tmdbId: Long): Box[Movie] = {
    (Movie where (_.tmdbId eqs tmdbId)).fetch(1) match {
      case Nil => {
        requestJSON(
          request(_ / "movie" / tmdbId.toString addQueryParameter("append_to_response", "trailers,images,casts,keywords"))
        ) match {
          case Full(raw) => Extractor.extractMovie(raw)
          case _         => Empty
        }
      }
      case _ => Empty
    }


  }




}
