package com.owlunit.web.snippet

import com.owlunit.web.model.User
import net.liftweb.util.Helpers._
import com.owlunit.web.config.Site
import com.owlunit.web.lib.ui.Gravatar
import com.owlunit.web.lib.AppHelpers
import net.liftweb.common.{Logger, Full, Failure, Empty}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object MeSnippet extends AppHelpers with Logger {

  def logout = "* [href]" #> url(Site.logout)

  def nameTag = ".name *" #> User.currentUser.map(_.snippet)

  def smallAvatar = "img" #> User.currentUser.map(user => Gravatar.imgTag(user.email.is, 30))

  def render =
    nameTag &
    ".picture-image   [src]" #> User.currentUser.map(user => user.photo.is) &
    ".wallpaper-image [src]" #> User.currentUser.map(user => user.cover.is) &
    ".occupation *"          #> "Some example from Scala" &
    ".rating [style]"        #> ("width: %d%%" format 95)

}