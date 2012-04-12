package com.owlunit.web.snippet.admin

import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import com.owlunit.web.lib._
import Helpers._
import com.owlunit.web.lib.CinemaUtilities._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import scala.xml.NodeSeq.seqToNodeSeq
import net.liftweb.http.js.JsCmds
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers.nextFuncName
import net.liftweb.http.{S, SHtml}
import collection.mutable.ListBuffer
import com.owlunit.service.cinema._
import stub._
import xml.{Null, Attribute, NodeSeq, Text}
import com.owlunit.core.ii.{IiDao, Recommender}
import com.owlunit.web.config.DependencyFactory

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class Item {

  lazy val cinemaService = DependencyFactory.inject[CinemaService] openOr CinemaServiceStub

  lazy val log = Logger(this.getClass)

  val item: Box[CinemaIi] = for {
    itemNo <- S.param("itemNo") ?~! "Item number is not present =/"
    id <- tryo{ itemNo.toLong } ?~! "Sould be a number"
    ii <- tryo{ cinemaService.load(id).get } ?~! "Item not found"
  } yield {
    ii
  }

  def renderMovie(movie: MovieIi) = {
    val movies = cinemaService.similarMovies(Map(movie -> 1));
    "item-type=movie [style]" #> "display: block;" &
      "item-data=type *" #> "MovieIi" &
      "item-data=name *" #> movie.name &
      "item-data=year *" #> movie.year.toString &
      "item-data=keyword *" #> movie.tags.map(t => "a *" #> t.name & "a [href]" #> ("/admin/item/" + t.id)) &
      "item-data=similar *" #> movies.map{
        case (m, w) => "a *" #> m.name &
          "a [href]" #> ("/admin/item/" + m.id) &
          "item-data=weight *" #> w.toString
      }
  }

  def renderKeyword(keyword: KeywordIi) = {
    "item-type=keyword [style]" #> "display: block;" &
      "item-data=type *" #> "KeywordIi" &
      "item-data=name *" #> keyword.name
  }

  def renderItem(item: CinemaIi) = item match {
    case x: MovieIi => renderMovie(x)
    case x: KeywordIi => renderKeyword(x)
    case x => "item-type=general [style]" #> "display: block;" & "item-data=detail *" #> x.detail
  }

  def render = if (item.isDefined) {
    val i = item.open_!
    "item-data=id *" #> i.id &
      renderItem(i)
  } else {
    NodeSeq.Empty
  }
}