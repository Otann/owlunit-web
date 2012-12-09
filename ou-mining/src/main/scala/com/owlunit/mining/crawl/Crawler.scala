package com.owlunit.mining.crawl

import com.weiglewilczek.slf4s.Logging
import org.bson.types.ObjectId
import net.liftweb.util.Props
import com.owlunit.web.config.{IiDaoConfig, MongoConfig}
import com.owlunit.web.model._

import net.liftweb.common.{Empty, Failure, Full}
import com.owlunit.crawl.parser.ParserHelper
import com.owlunit.crawl.CrawlerPaths
import com.owlunit.crawl.model._
import com.owlunit.mining.tmdb.TMDBApiClient

//import com.owlunit.crawl.parser.imdb._
import com.owlunit.crawl.parser.movielens.MoviesParser
import com.owlunit.crawl.lib.Counter
import com.foursquare.rogue.Rogue._


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */


object Crawler extends ParserHelper with CrawlerPaths with Logging {

  def main(args: Array[String]) {

    MongoConfig.init()
    IiDaoConfig.init()

    val total = 148322
//    val localLatest = 340
    val localLatest: Long = try {
      val movie = (Movie where (_.tmdbId > 0) orderDesc(_.tmdbId)).fetch(1).toList(0)
      movie.tmdbId.is
    } catch {
      case _: Throwable => 0
    }


    val counter = Counter.start(total - localLatest, 30, 1000 * 10)
    for (tmdbId <- localLatest + 1 to total) {
      try {
        TMDBApiClient.fetchNewMovie(tmdbId) match {
          case Full(movie)        => logger.debug("Processed #%s - %s"  format (tmdbId, movie.name))
          case Empty              => logger.debug("None loaded for #%s, skipping" format tmdbId)
          case Failure(msg, _, _) => logger.debug("Failed to fetch #%s" format tmdbId)
        }
      } catch {
        case t: Throwable => logger.debug("Unknown error saving #%s: %s" format (tmdbId, t))
      }

      counter.tick(logger, 30, "movies updated")
    }



  }

}