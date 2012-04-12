package bootstrap.liftweb

import net.liftweb._
import mongodb.{DefaultMongoIdentifier, MongoDB}
import util._
import Helpers._

import common._
import http._
import auth._
import sitemap._
import Loc._
import mapper._

import com.owlunit.web.config._
import com.owlunit.web.model.User

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def boot() {

    MongoConfig.init()
    DependencyFactory.init()
    LiftRules.unloadHooks.append(() => DependencyFactory.shutdown())

    LiftRules.addToPackages("com.owlunit.web")

    LiftRules.setSiteMapFunc(Site.sitemap _)

    LiftRules.statefulRewrite.append {
      case RewriteRequest(ParsePath("admin" :: "item" :: itemNo :: Nil,_,_,_),_,_) =>
        RewriteResponse("admin" :: "item" :: Nil, Map("itemNo" -> itemNo))
    }

    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts and go away when it ends
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Fade-out for notices
    LiftRules.noticesAutoFadeOut.default.set((noticeType: NoticeType.Value) => Full(1 seconds, 2 seconds))

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.isLoggedIn)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))


  }

}
