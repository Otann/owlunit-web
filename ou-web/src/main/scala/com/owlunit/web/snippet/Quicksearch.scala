package com.owlunit.web.snippet

import net.liftweb.util._
import net.liftweb.common._
import Helpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.{JArray, JValue}
import net.liftweb.http.SHtml
import com.owlunit.web.model.{Person, Keyword, Movie}
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
    Call("OU.Callbacks.receiveSearchedIi", items.toString)
  })

  private def loadItems(prefix: String) = JArray(
    Keyword.searchWithName(prefix).map(t => t.toJSON).take(10) :::
      Movie.searchWithName(prefix).map(t => t.toJSON).take(10) :::
      Person.searchWithName(prefix).map(t => t.toJSON).take(10)
  )

  def render = "name=search [oninput]" #> onChange

}
