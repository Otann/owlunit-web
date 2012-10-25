package com.owlunit.web.api

import com.owlunit.web.lib.AppHelpers
import com.owlunit.web.model.User
import net.liftweb._
import common._
import http._
import http.rest.RestHelper
import json.JsonDSL._
import util.Helpers._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object DropHandlerApiStateful extends RestHelper with AppHelpers with Loggable {

  serve( "api" / "drop" prefix {

    case "profile" :: Nil JsonPost json -> _ => for {
      user      <- User.currentUser ?~ "No user is logged in" ~> 500
      item_type <- extractString(json, _ \ "type") ?~ "No type provided" ~> 500
      item_id   <- extractString(json, _ \ "id") ?~ "No id provided" ~> 500
    } yield {
      logger.debug(json)
      item_type match {
        case "keyword" => JsonResponse(("result" -> "ok"))
        case _ => JsonResponse(("result" -> "fail"))
      }
    }

    case "trash" :: Nil JsonPost json -> _ => {
      // check source
      JsonResponse(("result" -> "not implemented"))
    }

    case "watchlist" :: Nil JsonPost json -> _ => {
      // use actors for update watchlists
      JsonResponse(("result" -> "not implemented"))
    }

    case "dropbar" :: Nil JsonPost json -> _ => {
      // use actors to update dropbar?
      JsonResponse(("result" -> "not implemented"))
    }


  })


}
