

package bootstrap.liftweb

import net.liftweb._
import common.Full
import common.Full._
import http._
import sitemap.Loc.Hidden
import sitemap.{SiteMap, Menu, Loc}
import util.{ NamedPF }


class Boot {
  def boot {
  
    // where to search snippet
    LiftRules.addToPackages("org.owls")

    // build sitemap
    val entries = List(
      Menu("Home") / "index",
      Menu("Search") / "search",
      Menu("Item") / "item" >> Hidden
    )
    LiftRules.setSiteMap(SiteMap(entries:_*))

    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate(ParsePath(List("exceptions","404"),"html",false,false))
    })

    //Show the spinny image when an Ajax call starts / ends
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    
    // set character encoding
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath("item" :: id :: Nil,"",true,_),_,_) => RewriteResponse("item" :: Nil, Map("id" -> id))
    }
  }
}