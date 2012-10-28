package com.owlunit.web.config

import net.liftweb._
import common.Full
import http._
import sitemap._
import sitemap.Loc._

import net.liftmodules.mongoauth.Locs
import com.owlunit.web.model.{Person, Movie, User}
import com.owlunit.web.lib.FacebookGraph
import util.Helpers

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object Site {

  object MenuGroup {
    val TopBar   = LocGroup("topbar")
    val Account  = LocGroup("account")
    val Admin    = LocGroup("admin")
  }

  object AuthLocs extends Locs {
    override protected def logoutLocParams =     MenuGroup.Account :: Hidden :: super.logoutLocParams
    override protected def loginTokenLocParams = MenuGroup.Account :: Hidden :: super.loginTokenLocParams
  }

  // locations (menu entries)
  val home = Menu("Home") / "index" >> MenuGroup.TopBar
  val error = Menu("Error")   / "error"

  val login = AuthLocs.buildLoginTokenMenu
  val logout = AuthLocs.buildLogoutMenu


  //  private val profileParamMenu = Menu.param[User](
//    "User",
//    "Profile",
//    User.findByUsername _,
//    _.username.is
//  ) / "profile" >> Loc.CalcValue(() => User.currentUser) >> MenuGroup.Account

  private val adminMenus =
    Menu("Admin") / "admin" / "index" submenus (
      Menu("Profile")       / "admin" / "profile",
      Menu("Movie")         / "admin" / "movie",
      Menu("Person")        / "admin" / "person",

      Menu("Create Movie")  / "admin" / "create" / "movie"  >> MenuGroup.Admin,
      Menu("Create Person") / "admin" / "create" / "person" >> MenuGroup.Admin
      )


  private def menus = List(
    home,
    logout,

//    profileParamMenu,
    Menu("Profile") / "me"  >> MenuGroup.TopBar >> If(() => S.loggedIn_?, "You must be logged in"),

    adminMenus >> MenuGroup.TopBar,

    Menu("About")   / "about"    >> MenuGroup.TopBar,
    Menu("Test")    / "test"     >> MenuGroup.TopBar,

    Menu("Movie")   / "movie"    >> Hidden,

    Menu("Throw")   / "throw"    >> Hidden,
    error                        >> Hidden,

    Menu.i("FacebookConnect") / "facebook" / "connect" >> EarlyResponse(() => {
      FacebookGraph.csrf(Helpers.nextFuncName)
      Full(RedirectResponse(FacebookGraph.authUrl, S.responseCookies: _*))
    }),

    Menu(Loc("Static", Link(List("static"), matchHead_? = true, url = "/static/index"), "Static Content"))
  )

  /*
  * Return a SiteMap needed for Lift
  */
  def sitemap: SiteMap = SiteMap(menus: _*)

  /*
  * Return a URL rewrites
  */
  val statefulRewrites: LiftRules.RewritePF = {

    case RewriteRequest(ParsePath("movie" :: id :: Nil,_,_,_),_,_) => {
      if (Movie.find(id).isDefined)
        RewriteResponse("movie" :: Nil, Map("id" -> id))
      else
        RewriteResponse("404" :: Nil)
    }

    case RewriteRequest(ParsePath("admin" :: "person" :: id :: Nil,_,_,_),_,_) => {
      if (Person.find(id).isDefined)
        RewriteResponse("person" :: Nil, Map("id" -> id))
      else
        RewriteResponse("404" :: Nil)
    }

  }

}