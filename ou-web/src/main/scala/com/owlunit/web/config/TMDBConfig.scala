package com.owlunit.web.config

import com.owlunit.web.lib.{JValueHelpers, AppHelpers}
import net.liftweb.util.Props
import dispatch._
import net.liftweb.common._
import com.owlunit.web.lib.JValueHelpers._

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object TMDBConfig extends AppHelpers with Loggable {

  val key = Props.get("tmdb.key", "N/A")
  val root = host("api.themoviedb.org") / "3" addQueryParameter("api_key", key)

  var baseUrl = "http://cf2.imgobject.com/t/p/"

  def init() {
    requestJSON(root / "configuration") match {
      case Full(raw) => {
        update[String](raw) (_ \ "images" \ "base_url") (baseUrl = _)
      }
      case _ => logger.error("Unable to update config from TMDB")
    }
  }

}
