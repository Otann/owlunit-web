package com.owlunit.mining.tmdb

import dispatch._
import net.liftweb.common._
import net.liftweb.json._
import net.liftweb.util.Props
import scala.Left
import com.ning.http.client.RequestBuilder
import scala.Left
import net.liftweb.common.Full
import scala.Right

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object TMDB extends Loggable {

  val key = Props.get("tmdb.key", "N/A")
  val root = host("api.themoviedb.org") / "3" addQueryParameter("api_key", key)

  def getJMovie(tmdbId: Long) = handleRequest(
    root / "movie" / tmdbId.toString addQueryParameter("append_to_response", "trailers,images,casts,keywords")
  )

  def getJConfig = handleRequest(root / "configuration")

  private def handleRequest(req: RequestBuilder): Box[JValue] =
    Http(req OK as.String).either() match {
      case Left(throwable) => Failure("Bad answer from TMDB", Full(throwable), Empty)
      case Right(value) => Full(JsonParser.parse(value))
    }

}
