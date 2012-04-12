package code.snippet

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import Helpers._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import com.owlunit.service.cinema.{KeywordIi, MovieService, KeywordService}
import com.owlunit.web.lib.stub.{MovieServiceStub, KeywordServiceStub}
import com.owlunit.web.config.DependencyFactory

class HelloWorld {

  lazy val keywordService = DependencyFactory.inject[KeywordService] openOr KeywordServiceStub
  lazy val movieService = DependencyFactory.inject[MovieService] openOr MovieServiceStub

  // replace the contents of the element with id "time" with the date
  def howdy = "#time *" #> Full("Not Robot")

  def keywordTest = ".tag *" #> keywordService.searchKeyword("Drama").map { keyword => ".ii *" #> keyword.name }

  val cmd: JsCmd = JsRaw("alert(’Button clicked’)")
  def ajaxTest = "#test" #> SHtml.ajaxButton(Text("Press me"), {() => Alert("Important Alert Goes Here!")})

}

