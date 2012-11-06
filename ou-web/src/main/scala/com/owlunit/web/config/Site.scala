package com.owlunit.web.config

import net.liftweb._
import common.Full
import http._
import sitemap._
import sitemap.Loc._

import net.liftmodules.mongoauth.Locs
import com.owlunit.web.model.{Person, Movie}
import com.owlunit.web.lib.FacebookGraph
import util.Helpers

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MenuGroups {
  val TopAdminBar    = LocGroup("admin_topbar")
  val LeftAdmin = LocGroup("admin_leftbar")
}

case class MenuLoc(menu: Menu) {
  lazy val url: String     = S.contextPath+menu.loc.calcDefaultHref
  lazy val fullUrl: String = S.hostAndPath+menu.loc.calcDefaultHref
}

object Site extends Locs {
  import MenuGroups._

  // Authentication
  override protected def logoutLocParams =     Hidden :: super.logoutLocParams
  override protected def loginTokenLocParams = Hidden :: super.loginTokenLocParams

  // Globaly uses locations
  val home = MenuLoc(Menu.i("Home") / "index" >> TopAdminBar)
  val error = MenuLoc(Menu.i("Error")   / "error" >> Hidden)

  // Auth locations
  val loginToken = MenuLoc(super.buildLoginTokenMenu)
  val logout = MenuLoc(super.buildLogoutMenu)

  // Administration part
  private val adminMenus =
    Menu.i("Admin") / "admin" / "index" submenus (

      Menu.i("Movie")         / "admin" / "movie",
      Menu.i("Person")        / "admin" / "person",
      Menu.i("Keyword")       / "admin" / "keyword"

      )

  // Website root menu
  /////////////////////////

  private def menus = List(

    // Basic menus
    home.menu,
    error.menu,
    logout.menu,
    loginToken.menu,
    Menu.i("Login") / "login" >> RequireNotLoggedIn,

    // User space menus
    Menu("Profile")         / "me"  >> TopAdminBar >> RequireLoggedIn,
    Menu("Recommendations") / "recommendation",

    // Rewrite menus for objects
    Menu("Movie")           / "movie" >> Hidden,

    // API menus
    Menu.i("FacebookConnect") / "facebook" / "connect" >> EarlyResponse(() => {
      FacebookGraph.csrf(Helpers.nextFuncName)
      Full(RedirectResponse(FacebookGraph.authUrl, S.responseCookies: _*))
    }) >> Hidden,

    // Admin menus
    adminMenus >> TopAdminBar >> HasRole("admin"),

    // Static files
    Menu(Loc("Static", Link(List("static"), matchHead_? = true, url = "/static/index"), "Static Content", Hidden))

  )

  // SiteMap needed for Lift
  def sitemap: SiteMap = SiteMap(menus: _*)

  // URL rewrites
  val statefulRewrites: LiftRules.RewritePF = {

    case RewriteRequest(ParsePath("movie" :: id :: Nil, _, _, _), _, _) => RewriteResponse("movie" :: Nil, Map("id" -> id))

    case RewriteRequest(ParsePath("admin" :: "movie"   :: id :: Nil, _, _, _), _, _) => RewriteResponse("admin" :: "movie"   :: Nil, Map("id" -> id))
    case RewriteRequest(ParsePath("admin" :: "person"  :: id :: Nil, _, _, _), _, _) => RewriteResponse("admin" :: "person"  :: Nil, Map("id" -> id))
    case RewriteRequest(ParsePath("admin" :: "keyword" :: id :: Nil, _, _, _), _, _) => RewriteResponse("admin" :: "keyword" :: Nil, Map("id" -> id))

  }

}