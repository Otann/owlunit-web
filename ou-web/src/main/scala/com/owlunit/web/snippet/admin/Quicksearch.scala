package com.owlunit.web.snippet.admin

import net.liftweb.util._
import net.liftweb.common._
import com.owlunit.web.lib._
import Helpers._
import net.liftweb.http.js.{JsExp, JsObj, JsCmd, JsCmds}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import scala.xml.NodeSeq.seqToNodeSeq
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers.nextFuncName
import net.liftweb.http.{S, SHtml}
import collection.mutable.ListBuffer
import xml.{Null, Attribute, NodeSeq, Text}
import com.owlunit.web.config.DependencyFactory
import java.util.{Collections, Date}
import com.owlunit.web.model.{IiMongoRecord, Person, Keyword, Movie}
import org.bson.types.ObjectId
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JE.JsObj
import net.liftweb.http.js.JsObj
import xml.Text
import java.util

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
object QuickSearch extends Loggable {

  lazy val log = Logger(this.getClass)

  val minChars = 3
  val resultsId = "quicksearch-results"

  def onChange = SHtml.onEvent(prefix => {
    val items = if (prefix.length >= minChars) loadItems(prefix) else JsNull
    Call("OU.Callbacks.receiveSearchedIi", items)
  })

  private def makeSimpleIi(id: String, hrefPrefix: String, caption: NodeSeq) =
      <span class="ii" href={ hrefPrefix + id }>{ caption }</span> % Attribute(None, "data-itemId", Text(id), Null)

  private def loadItems(prefix: String): JsExp = {
    // t.id.is
    // t.name.is
     val items = List[JsObj]() ++
      Keyword.searchByName(prefix).map(t => t.toTagJSON) ++
      Movie.searchByName(prefix).map(t => t.toTagJSON)
    JsArray(items)
  }

  def render = {
      "name=search [oninput]" #> onChange &
      ".dropdown-menu [id]" #> resultsId &
      "form [class]" #> "navbar-search pull-right" &
      ".test *" #> Script(Call("OU.Callback.test", JsObj(("name", "Thor"), ("race", "Asgard"))))
  }

}
