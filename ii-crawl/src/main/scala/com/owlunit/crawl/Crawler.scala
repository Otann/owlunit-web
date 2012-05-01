package com.owlunit.crawl

import com.owlunit.core.ii.immutable.IiDao
import imdb.{PersonsCrawler, KeywordsCrawler}
import movielens.MoviesCrawler
import org.apache.log4j.Level
import org.neo4j.rest.graphdb._
import com.owlunit.service.cinema._
import com.weiglewilczek.slf4s.Logging

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object Crawler extends Logging {

  val moviesPath      = "runtime/movielens/movies.dat"
  val keywordsPath    = "runtime/imdb/keywords.list"
  val actorsPath      = "runtime/imdb/actors.list"
  val actressesPath   = "runtime/imdb/actresses.list"
  val directorsPath   = "runtime/imdb/directors.list"
  val producersPath   = "runtime/imdb/producers.list"

  val dao = IiDao.local("/Users/anton/Dev/Owls/data")
//  val dao = IiDao.remote("http://04e118aa4.hosted.neo4j.org:7034/db/data/", "a9786d4e8", "b72321c25")

  val cinemaService = CinemaService(dao)

  def main(args: Array[String]) {

//    new MoviesCrawler(moviesPath, cinemaService).run()
//    new KeywordsCrawler(keywordsPath, cinemaService).run()

    new PersonsCrawler(actorsPath, cinemaService, Role.Actor, 12338129).run()
    new PersonsCrawler(actressesPath, cinemaService, Role.Actor, 7243872).run()
    new PersonsCrawler(directorsPath, cinemaService, Role.Director, 1724653).run()
    new PersonsCrawler(producersPath, cinemaService, Role.Producer, 3719561).run()

//    log.debug(keywordService.loadOrCreate("Drama 1").toString)
//    log.debug(keywordService.loadOrCreate("Drama 2").toString)
//    log.debug(keywordService.search("dra").mkString("\n", "\n", "\n"))
    
    dao.shutdown()
  }

}