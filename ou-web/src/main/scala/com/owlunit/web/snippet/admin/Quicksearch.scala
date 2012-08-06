package com.owlunit.web.snippet.admin

import net.liftweb.util._
import net.liftweb.common._
import com.owlunit.web.lib._
import Helpers._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import scala.xml.NodeSeq.seqToNodeSeq
import net.liftweb.http.js.JsCmds
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers.nextFuncName
import net.liftweb.http.{S, SHtml}
import collection.mutable.ListBuffer
import xml.{Null, Attribute, NodeSeq, Text}
import com.owlunit.web.config.DependencyFactory
import java.util.{Collections, Date}
import com.owlunit.web.model.{IiMongoRecord, Person, Keyword, Movie}
import org.bson.types.ObjectId

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
object QuickSearch extends Loggable {

  lazy val log = Logger(this.getClass)

  val minChars = 3
  val resultsId = "quicksearch-results"

  def onChange = SHtml.onEvent(prefix => {
    if (prefix.length >= 3) {
      val items = loadItems(prefix)
      if (items.length > 0) JsCmds.SetHtml(resultsId, items) & JsCmds.JsShowId(resultsId) else JsCmds.Noop
    } else {
      JsCmds.SetHtml(resultsId, NodeSeq.Empty) & JsCmds.JsHideId(resultsId)
    }
  })

  private def makeSimpleIi(id: String, hrefPrefix: String, caption: NodeSeq) =
      <span class="ii" href={ hrefPrefix + id }>{ caption }</span> % Attribute(None, "data-itemId", Text(id), Null)

  private def loadItems(prefix: String) = {
    NodeSeq.Empty ++
      Keyword.searchByName(prefix).map(t => <li>{ makeSimpleIi(t.id.is.toString, "/admin/keyword/", Text(t.name.is.toString)) }</li>) ++
      Movie.searchByName(prefix).map(t => <li>{ makeSimpleIi(t.id.is.toString, "/admin/movie/", Text(t.name.is.toString)) }</li>)
  }

  def render = {
      "name=search [oninput]" #> onChange &
      ".dropdown-menu [id]" #> resultsId &
      "form [class]" #> "navbar-search pull-right"
  }

}
