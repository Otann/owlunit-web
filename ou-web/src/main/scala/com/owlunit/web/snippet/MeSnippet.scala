package com.owlunit.web.snippet

import com.owlunit.web.model.User
import net.liftweb.util.Helpers._
import com.owlunit.web.config.Site
import com.owlunit.web.lib.AppHelpers
import net.liftweb.common.Logger

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object MeSnippet extends AppHelpers with Logger {

  def logout = "* [href]" #> url(Site.logout)

  def smallAvatar = "img [src]" #> User.currentUser.map(_.photo.is)

  def render =
    ".name *"                #> User.currentUser.map(_.snippet) &
    ".picture-image   [src]" #> User.currentUser.map(_.photo.is) &
    ".wallpaper-image [src]" #> User.currentUser.map(_.cover.is) &
    ".occupation *"          #> User.currentUser.map(_.bio.is) &
    ".rating [style]"        #> ("width: %d%%" format 95)

}