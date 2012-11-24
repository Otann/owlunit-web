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
import com.owlunit.web.config.DependencyFactory.iiDao

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */

object DropHandlerApiStateful extends RestHelper with AppHelpers with Loggable {

  serve( "api" / "drop" prefix {

    case "profile" :: Nil JsonPost json -> _ => {
      val response = for {
        user <- User.currentUser      ?~ "No user is logged in" ~> 500
        tag  <- IiTag.fromJSON(json)  ?~ "Unable to parse json" ~> 500
        item <- IiTagRecord.load(tag) ?~ "Unable to find item"  ~> 500
      } yield {
        user.addTag(item).save
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


    case "admin" :: fromId :: toId :: Nil Get _ => {
      val response = for {
        user     <- User.currentUser          ?~ "No user is logged in"   ~> 500
        if User.hasRole("admin")
        fromTag  <- IiTagRecord.load(fromId)  ?~ "Item 'from' not found"  ~> 404
        fromItem <- IiTagRecord.load(fromTag) ?~ "Item 'from' not found"  ~> 404
        toTag    <- IiTagRecord.load(toId)    ?~ "Item 'to' not found"    ~> 404
        toItem   <- IiTagRecord.load(toTag)   ?~ "Item 'to' not found"    ~> 404
      } yield {
        val fromIi = iiDao.vend.load(fromItem.ii.id)
        val toIi   = iiDao.vend.load(toItem.ii.id)
        fromIi.setItem(toIi, 239.0).save
        OkResponse()
      }
      response
    }


  })


}
