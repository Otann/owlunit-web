package com.owlunit.web.snippet.admin

import net.liftweb.util._
import net.liftweb.common._
import Helpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.SHtml
import com.owlunit.web.model.{Keyword, Movie}
import net.liftweb.http.js.JE.Call

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
object Quicksearch extends Loggable {

  lazy val log = Logger(this.getClass)

  val minChars = 3
  val resultsId = "quicksearch-results"

  def onChange = SHtml.onEvent(prefix => {
    val items = if (prefix.length >= minChars) loadItems(prefix) else JsArray()
    Call("OU.Callbacks.receiveSearchedIi", items)
  })

  private def loadItems(prefix: String) = JsArray(
    Keyword.searchWithName(prefix).map(t => t.toTagJSON) :::
    Movie.searchByName(prefix).map(t => t.toTagJSON)
  )

  def render = "name=search [oninput]" #> onChange

}
