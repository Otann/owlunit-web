package bootstrap.liftweb

import net.liftweb._
import util._

import common._
import http._
import net.liftmodules.JQueryModule

import com.owlunit.web.config._
import com.owlunit.web.model.User
import com.owlunit.web.api.{SearchApiStateful, DropHandlerApiStateful, FacebookApiStateful}
import net.liftmodules.mongoauth.MongoAuth

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def boot() {

    TMDBConfig.init()
    MongoConfig.init()
    DependencyFactory.init()
    LiftRules.unloadHooks.append(() => IiDaoConfig.shutdown())

    LiftRules.addToPackages("com.owlunit.web")

    LiftRules.setSiteMapFunc(Site.sitemap _)

    LiftRules.statefulRewrite.append(Site.statefulRewrites) // add rewrites

    LiftRules.dispatch.append(FacebookApiStateful)    // register Facebook API
    LiftRules.dispatch.append(DropHandlerApiStateful) // register Drag'n'Drop API
    LiftRules.dispatch.append(SearchApiStateful)      // register Search API

    // init mongoauth ?
    MongoAuth.authUserMeta.default.set(User)
    MongoAuth.indexUrl.default.set(Site.home.url)

    JQueryModule.InitParam.JQuery = JQueryModule.JQuery172
    JQueryModule.init()

    //Show the spiny image when an Ajax call starts and go away when it ends
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Fade-out for notices
//    LiftRules.noticesAutoFadeOut.default.set((noticeType: NoticeType.Value) => Full(1 seconds, 2 seconds))

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.isLoggedIn)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate(ParsePath(List("404"), "html", absolute = false, endSlash = false))
    })

  }

}
