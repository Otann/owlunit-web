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

  def username = "* *" #> User.currentUser.map(_.username.is)

  def logout = "* [href]" #> url(Site.logout)

  def nameTag = ".name *" #> User.currentUser.map(_.snippet)

  def render =
    nameTag &
    ".picture *" #> User.currentUser.map(user => Gravatar.imgTag(user.email.is)) &
    ".wallpaper [style]" #> "background: url(http://placehold.it/606x60)" &
    ".occupation *" #> "Some example from Scala" &
    ".rating [style]" #> "width: 95%"

}