package com.owlunit.web.snippet

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import stub.{MovieServiceStub, KeywordServiceStub}
import code.lib.CinemaUtilities._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.NodeSeq
import net.liftweb.http.js.JsCmds
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers.nextFuncName
import net.liftweb.http.{S, SHtml}
import com.owlunit.service.cinema.{Movie, Keyword, MovieService, KeywordService}
import util.Random

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Movies {

  lazy val keywordService = DependencyFactory.inject[KeywordService] openOr KeywordServiceStub
  lazy val movieService = DependencyFactory.inject[MovieService] openOr MovieServiceStub
  
  lazy val backdrops = List("")
  lazy val prefix = "Matrix"
  lazy val rand = new Random(System.currentTimeMillis())

  def render = {

    backdrops

    val movies = movieService.search(prefix)
    val max = movies.foldLeft(0)((i, m) => i max m.tags.size)

    val keyword = keywordService.loadOrCreate("Drama")

    movies.foreach(movieService.addKeyword(_, keyword, 300))

    "*" #> movies.map{movie =>
      "ii-helper=movie-name *" #> movie.name &
      "ii-helper=tag *" #> { movie.tags.map(renderKeyword(_)) } &
      "ii-helper=percentage [style]" #> { "width: %d%%" format (100 * movie.tags.size / max) } &
      "ii-helper=backdrop [style]" #> { "width: %d%%" format (100 * movie.tags.size / max) }
    }
  }

}