package com.owlunit.web.api

import com.owlunit.web.lib.AppHelpers
import com.owlunit.web.model.{Person, Keyword, Movie}
import net.liftweb._
import common._
import http.rest.RestHelper
import json.JsonAST.JArray
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.model.common.IiTagRecord

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object SearchApiStateful extends RestHelper with AppHelpers with Loggable {

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO(Anton) unsafe vend

  serve {
    case "api" :: "search" :: query :: Nil Get _ => JArray(IiTagRecord.search(query).map(_.toJSON).toList)
  }

  serve( "api" / "search" prefix {
    case query :: Nil Get _ => JArray(IiTagRecord.search(query).map(_.toJSON).toList)
    case "keywords" :: query :: Nil JsonGet _ => JArray(Keyword.searchWithName(query).map(_.toJSON))
    case "persons" :: query :: Nil JsonGet _ => JArray(Person.searchWithName(query).map(_.toJSON))
    case "movies" :: query :: Nil JsonGet _ => JArray(Movie.searchWithName(query).map(_.toJSON))
  })

}
