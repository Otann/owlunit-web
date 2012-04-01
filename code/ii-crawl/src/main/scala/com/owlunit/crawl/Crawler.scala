package com.owlunit.crawl

import imdb.KeywordsCrawler
import com.owlunit.core.ii.IiDao
import com.codahale.logula.Logging
import movielens.MoviesCrawler
import org.apache.log4j.Level
import com.owlunit.service.cinema.{Keyword, PersonService, KeywordService, MovieService}
import org.neo4j.rest.graphdb._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object Crawler extends Logging {

  val moviesPath      = "runtime/movielens/movies.dat"
  val keywordsPath    = "runtime/imdb/keywords.list"

  val dao = IiDao.local("/Users/anton/Dev/Owls/data")
//  val dao = IiDao.remote("http://04e118aa4.hosted.neo4j.org:7034/db/data/", "a9786d4e8", "b72321c25")

  val keywordService = KeywordService(dao)
  val personService = PersonService(dao)
  val movieService = MovieService(dao)

  Logging.configure { log =>
    log.level = Level.INFO
    log.loggers("com.owlunit.crawl") = Level.TRACE

    log.console.enabled = true
    log.console.threshold = Level.TRACE
    log.console.formatted("")
  }

  def main(args: Array[String]) {

    new MoviesCrawler(moviesPath, movieService, keywordService).run()
    new KeywordsCrawler(keywordsPath, movieService, keywordService).run()

//    log.debug(keywordService.loadOrCreate("Drama 1").toString)
//    log.debug(keywordService.loadOrCreate("Drama 2").toString)
//    log.debug(keywordService.search("dra").mkString("\n", "\n", "\n"))
    
    dao.shutdown()
  }

}