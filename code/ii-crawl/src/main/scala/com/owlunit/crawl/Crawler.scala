package com.owlunit.crawl

import imdb.KeywordsCrawler
import io.Source
import com.owlunit.core.ii.IiDao
import com.codahale.logula.Logging
import movielens.MoviesCrawler
import org.apache.log4j.Level
import com.owlunit.service.cinema.impl.{KeywordServiceImpl, PersonServiceImpl, MovieServiceImpl}
import com.owlunit.service.cinema.{Keyword, PersonServiceImpl, KeywordServiceImpl, MovieServiceImpl}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object Crawler extends Logging {

  val Neo4jPath = "/Users/anton/Dev/Owls/data"

  val moviesPath  = "runtime/movielens/movies.dat"
  val keywordsPath  = "runtime/imdb/keywords.list"

  val dao = IiDao(Neo4jPath)

  val keywordService = KeywordServiceImpl(dao)
  val personService = PersonServiceImpl(dao)
  val movieService = MovieServiceImpl(dao)

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

  }

}