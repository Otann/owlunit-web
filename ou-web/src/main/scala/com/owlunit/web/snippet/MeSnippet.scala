package com.owlunit.web.snippet

import com.owlunit.web.model.User
import net.liftweb.util.Helpers._
import com.owlunit.web.config.Site
import com.owlunit.web.lib.{Gravatar, AppHelpers}
import net.liftweb.common.{Logger, Full, Failure, Empty}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object MeSnippet extends AppHelpers with Logger {

  val owlUrl = "http://i293.photobucket.com/albums/mm46/smiley_foreva/Badge/owl.png"
  val failUrl = "http://sisyphus.ru/img/fail.png"

  def username = "* *" #> {
    User.currentUser match {
      case Empty => "NONAME"
      case Failure(msg, _, _) => msg
      case Full(user) => user.username.is
    }
  }

  def logout = "* [href]" #> url(Site.logout)

  def current = {
    val button = <button class="btn btn-primary">Print</button>
    "*" #> (button ++ User.currentUser.map(_.toForm(button)(debug(_))))
  }

  def nameTag = ".name *" #> User.currentUser.map(_.render)

  def render =
    nameTag &
    ".picture *" #> User.currentUser.map(user => Gravatar.imgTag(user.email.is)) &
    ".wallpaper [style]" #> "background: url(http://placehold.it/606x60)" &
    ".occupation *" #> "Some example from Scala" &
    ".rating [style]" #> "width: 95%"


  //
}