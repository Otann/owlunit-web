package com.owlunit.web.config

import net.liftweb._
import common._
import http.S
import sitemap._
import sitemap.Loc._

import net.liftmodules.mongoauth.Locs._
import com.owlunit.web.model.User

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object Site {

  object MenuGroups {
    val SettingsGroup = LocGroup("settings")
    val TopBarGroup = LocGroup("topbar")
  }

  case class MenuLoc(menu: Menu) {
    lazy val url     = S.contextPath + menu.loc.calcDefaultHref
    lazy val fullUrl = S.hostAndPath + menu.loc.calcDefaultHref
  }

  // locations (menu entries)
    val home = MenuLoc(Menu("Home") / "index" >> MenuGroups.TopBarGroup)
    val loginToken = MenuLoc(buildLoginTokenMenu)
    val logout = MenuLoc(buildLogoutMenu)
    private val profileParamMenu = Menu.param[User]("User", "Profile",
      User.findByUsername _,
      _.username.is
    ) / "user" >> Loc.CalcValue(() => User.currentUser)
    lazy val profileLoc = profileParamMenu.toLoc

    val password =    MenuLoc(Menu("Password") / "settings" / "password" >> RequireLoggedIn >> MenuGroups.SettingsGroup)
    val account =     MenuLoc(Menu("Account") / "settings" / "account" >> MenuGroups.SettingsGroup >> RequireLoggedIn)
    val editProfile = MenuLoc(Menu("EditProfile", "Profile") / "settings" / "profile" >> MenuGroups.SettingsGroup >> RequireLoggedIn)
    val register =    MenuLoc(Menu("Register") / "register" >> RequireNotLoggedIn)

    private def menus = List(
      home.menu,
      Menu.i("Login")   / "login"    >> RequireNotLoggedIn,
      register.menu,
      loginToken.menu,
      logout.menu,
      profileParamMenu,
      account.menu,
      password.menu,
      editProfile.menu,
      Menu("About")   / "about"    >> MenuGroups.TopBarGroup,
      Menu("Contact") / "contact"  >> MenuGroups.TopBarGroup,
      Menu("Throw")   / "throw"    >> Hidden,
      Menu("Error")   / "error"    >> Hidden,
      Menu("404")     / "404"      >> Hidden,

      Menu("Admin") / "admin",
      Menu("Admin Item") / "admin" / "item" >> Hidden
    )

    /*
     * Return a SiteMap needed for Lift
     */
    def sitemap: SiteMap = SiteMap(menus:_*)

}