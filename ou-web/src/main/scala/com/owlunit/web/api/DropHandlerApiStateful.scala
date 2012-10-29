package com.owlunit.web.api

import com.owlunit.web.lib.AppHelpers
import com.owlunit.web.model.User
import net.liftweb._
import common._
import http._
import http.rest.RestHelper
import json.JsonDSL._
import util.Helpers._
import com.owlunit.web.lib.ui.IiTag
import com.owlunit.web.model.common.IiTagRecord

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object DropHandlerApiStateful extends RestHelper with AppHelpers with Loggable {

  serve( "api" / "drop" prefix {

    case "profile" :: Nil JsonPost json -> _ => {
      val response = for {
        user <- User.currentUser ?~ "No user is logged in" ~> 500
        tag  <- IiTag.fromJSON(json)  ?~ "Unable to parse json" ~> 500
        item <- IiTagRecord.load(tag) ?~ "Unable to find item" ~> 500
      } yield {
        user.addTag(item).save
        logger.debug("adding ii: %s" format item.ii)
        logger.debug("user items: %s" format user.ii.items)
        OkResponse()
      }
      response // makes less implicit highlighting
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
